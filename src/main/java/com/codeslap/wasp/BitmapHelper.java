package com.codeslap.wasp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Debug;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helper to deal with Bitmaps, including downloading and caching.
 *
 * @author evelio
 * @author cristian
 * @version 1.1
 */
public class BitmapHelper {
    /**
     * Unique instance of this helper
     */
    private static BitmapHelper instance;
    /**
     * On memory pool to add already loaded from file bitmaps
     * Note: will be purged on by itself in case of low memory
     */
    private final BitmapRefCache cache;
    /**
     * the hard worker
     */
    private final BitmapLoader loader;

    /**
     * Unique constructor
     * must be quick as hell
     */
    private BitmapHelper() {
        cache = new BitmapRefCache();
        loader = new BitmapLoader();
    }

    /**
     * Singleton method
     *
     * @return {@link #instance}
     */
    public static BitmapHelper getInstance() {
        if (instance == null) {
            instance = new BitmapHelper();
        }
        return instance;
    }

    /**
     * Clears current cache if any
     */
    public void clearCache() {
        if (cache != null) {
            cache.evictAll();
        }
    }

    /**
     * Try to get the bitmap from cache
     *
     * @param urlFrom A valid URL pointing to a bitmap
     * @return A bitmap associated to given url if any available or will try to download it
     *         <p/>
     *         Note: in case of urlFrom parameter is null this method does nothing
     */
    public Bitmap getBitmap(String urlFrom) {
        if (isInvalidUri(urlFrom)) {
            return null;
        }
        //Lets check the cache
        BitmapRef ref = cache.get(urlFrom);
        if (ref != null) {
            return ref.getBitmap();
        }
        return null;
    }

    /**
     * Try to get the bitmap from cache
     *
     * @param context used to get the cache directory
     * @param urlFrom A valid URL pointing to a bitmap
     * @return A bitmap associated to given url if any available or will try to download it
     *         <p/>
     *         Note: in case of urlFrom parameter is null this method does nothing
     */
    public Bitmap getBitmapFromCacheDir(Context context, String urlFrom) {
        if (isInvalidUri(urlFrom)) {
            return null;
        }
        Bitmap bitmap = getBitmap(urlFrom);
        if (BitmapUtils.isBitmapValid(bitmap)) {
            // bitmap was already cached, just return it
            return bitmap;
        }
        // bitmap is not cached, let's see if it is persisted in the cache directory
        File cacheDirectory = IOUtils.getCacheDirectory(context);
        String filename = String.valueOf(urlFrom.hashCode());
        File file = new File(cacheDirectory, filename);
        if (file.exists()) {
            // file is there... let's try to decode it
            try {
                bitmap = BitmapUtils.loadBitmapFile(file.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (BitmapUtils.isBitmapValid(bitmap)) {
                BitmapRef bitmapRef = new BitmapRef(urlFrom);
                bitmapRef.loaded(bitmap);
                cache.put(urlFrom, bitmapRef);
                return bitmap;
            }
        }
        return null;
    }

    /**
     * Register a bitmap in the cache system.
     *
     * @param context used to get the cache directory
     * @param bitmap  the bitmap to save to cache
     * @param uri     the unique resource identifier to this cache
     * @param persist true if the bitmap should be persisted to file system
     */
    public void cacheBitmap(final Context context, final Bitmap bitmap, final String uri, boolean persist) {
        if (!BitmapUtils.isBitmapValid(bitmap)) {
            return;
        }

        if (persist) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String filename = String.valueOf(uri.hashCode());
                    File file = new File(IOUtils.getCacheDirectory(context), filename);
                    try {
                        FileOutputStream stream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        stream.close();
                    } catch (Exception ignored) {
                    }
                }
            }).start();
        }

        BitmapRef bitmapRef = new BitmapRef(uri);
        bitmapRef.bitmapRef = bitmap;
        cache.put(uri, bitmapRef);
    }

    /**
     * Try to get a list of Bitmaps, if any of them is already on cache given observer will be
     * notified about right away, those not in cache will be loaded later and observer will get it
     *
     * @param context  Context to use
     * @param urls     List of URL to download/load from
     * @param observer Will be notified on bitmap loaded
     */
    public void bulkBitmaps(Context context, List<String> urls, BaseBitmapObserver observer) {
        if (observer == null || urls == null || urls.size() < 1) {
            return;
        }
        for (String url : urls) {
            observer.setTakeUriIntoAccount(false);// since the same observer will be used for all urls
            registerBitmapObserver(context, url, observer, null);
        }
    }

    /**
     * Download and put in cache a bitmap
     *
     * @param context  Context to use
     * @param urlFrom  A valid URL pointing to a bitmap
     * @param observer Will be notified on bitmap loaded
     */
    public void registerBitmapObserver(Context context, String urlFrom, BaseBitmapObserver observer, com.codeslap.wasp.BitmapLoader fileLoader) {
        if (isInvalidUri(urlFrom)) {
            return;
        }
        //Lets check the cache
        BitmapRef ref = cache.get(urlFrom);
        Bitmap bitmap = null;
        if (ref == null) {
            //Hummm nothing in cache lets try to put it in cache
            ref = new BitmapRef(urlFrom);
            cache.putAndObserve(urlFrom, ref);
        } else {
            bitmap = ref.getBitmap();
        }

        if (!BitmapUtils.isBitmapValid(bitmap)) { //humm garbage collected or not already loaded lest try to load it anyway
            ref.addObserver(observer);
            if (fileLoader != null) {
                ref.setLoader(fileLoader);
            }
            loader.load(context, ref);
        } else {
            observer.update(ref, null); // We got a valid ref and bitmap let's the observer know
        }
    }

    /**
     * Download and put in cache a bitmap
     *
     * @param context  Context to use
     * @param observer Will be notified on bitmap loaded
     */
    public void registerBitmapObserver(Context context, BaseBitmapObserver observer) {
        registerBitmapObserver(context, observer.getUrl(), observer, null);
    }

    public void registerBitmapObserver(Context context, BaseBitmapObserver observer, com.codeslap.wasp.BitmapLoader fileLoader) {
        registerBitmapObserver(context, observer.getUrl(), observer, fileLoader);
    }

    private static boolean isInvalidUri(String url) {
        return url == null || url.length() == 0;
    }

    /**
     * Wrapper to an association between an URL and a in memory cached bitmap
     * <p/>
     * URL must be immutable.
     *
     * @author evelio
     */
    static class BitmapRef extends Observable {
        Bitmap bitmapRef;
        String from;
        Observer stickyObserver;
        int currentSize;
        int previousSize;
        private com.codeslap.wasp.BitmapLoader mFileLoader;

        /**
         * Creates a new instance with given uri
         *
         * @param uri a bitmap url
         */
        public BitmapRef(String uri) {
            if (isInvalidUri(uri)) {
                throw new IllegalArgumentException("Invalid URL");
            }
            from = uri;
            currentSize = previousSize = 0;
        }

        /**
         * @return Bitmap cached or null if was garbage collected
         */
        public Bitmap getBitmap() {
            return bitmapRef;
        }

        /**
         * @return URL associated to this BitmapRef
         */
        public String getUri() {
            return from;
        }

        public int getCurrentSize() {
            return currentSize;
        }

        public int getPreviousSize() {
            return previousSize;
        }

        public void setLoader(com.codeslap.wasp.BitmapLoader fileLoader) {
            mFileLoader = fileLoader;
        }

        public com.codeslap.wasp.BitmapLoader getLoader() {
            return mFileLoader;
        }

        @Override
        public int hashCode() {
            return from.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BitmapRef) {
                BitmapRef otherRef = (BitmapRef) obj;
                return from.equals(otherRef.getUri());
            }
            return false;
        }

        /**
         * @param bmp Bitmap to associate
         */
        public void loaded(Bitmap bmp) {
            previousSize = currentSize;
            currentSize = BitmapUtils.getBitmapSize(bmp);
            bitmapRef = bmp;

            setChanged();
            notifyObservers();
            deleteObservers();
        }

        @Override
        public String toString() {
            return super.toString() + "{ "
                    + "bitmap: " + getBitmap()
                    + "from: " + from
                    + " }";
        }

        public void setStickyObserver(Observer sticky) {
            if (stickyObserver != null) {
                if (stickyObserver.equals(sticky)) {
                    return;
                } else {
                    deleteObserver(stickyObserver);
                }
            }
            stickyObserver = sticky;
            if (sticky != null) {
                addObserver(sticky);
            }
        }

        @Override
        public void deleteObservers() {
            super.deleteObservers();
            if (stickyObserver != null) {
                addObserver(stickyObserver);
            }
        }

        /**
         * Removes any reference to hard referenced bitmap and observers
         */
        public void recycle() {
            stickyObserver = null;
            deleteObservers();
            bitmapRef = null;
        }
    }

    /**
     * Calls that makes the dirty work
     *
     * @author evelio
     */
    private static class BitmapLoader {

        private final ExecutorService executor;
        /**
         * reference to those already queued
         */
        private final Set<BitmapRef> queued;
        /**
         * Directory to use as a file cache
         */
        private File cacheDir;

        /**
         * Default constructor
         */
        private BitmapLoader() {
            executor = Executors.newCachedThreadPool();
            queued = Collections.synchronizedSet(new HashSet<BitmapRef>());
        }

        /**
         * Loads a Bitmap into the given ref
         *
         * @param context context needed to get app cache directory
         * @param ref     Reference to use
         */
        private void load(Context context, BitmapRef ref) {
            if (ref == null || BitmapUtils.isBitmapValid(ref.getBitmap())) {
                return;
            }

            if (cacheDir == null) {
                cacheDir = IOUtils.getCacheDirectory(context);
            }

            if (queued.add(ref)) {
                executor.execute(new LoadTask(context, ref, cacheDir));
            }
        }


        private class LoadTask implements Runnable {
            private static final String TAG = "BitmapHelper.LoadTask";
            private final Context mContext;
            private final BitmapRef reference;
            private final File cacheDir;

            private LoadTask(Context context, BitmapRef ref, File cacheDir) {
                mContext = context;
                reference = ref;
                this.cacheDir = cacheDir;
            }

            @Override
            public void run() {
                try {
                    //load it
                    Bitmap bmp = doLoad();
                    reference.loaded(bmp);
                } catch (Exception e) {
                    if (e != null) {
                        Log.e(TAG, "Unable to load bitmap", e);
                    }
                }
                queued.remove(reference);
            }

            private Bitmap doLoad() throws IOException {
                Bitmap image = null;

                String filename = String.valueOf(reference.hashCode());
                File file = new File(cacheDir, filename);

                if (file.exists()) {//Something is stored
                    image = BitmapUtils.loadBitmapFile(file.getCanonicalPath());
                }

                if (image == null) {//So far nothing is cached, lets download it
                    if (reference.getLoader() != null) {
                        reference.getLoader().load(mContext, reference.getUri(), file);
                    } else {
                        IOUtils.downloadFile(mContext, reference.getUri(), file);
                    }
                    if (file.exists()) {
                        image = BitmapUtils.loadBitmapFile(file.getCanonicalPath());
                    }
                }
                return image;
            }
        }
    }

    private static class BitmapRefCache extends UpdateableLruCache<String, BitmapRef> {
        private static final float DESIRED_PERCENTAGE_OF_MEMORY = 0.25f;
        private static final int BYTES_IN_A_MEGABYTE = 1048576;
        private static final int MINIMAL_MAX_SIZE = BYTES_IN_A_MEGABYTE * 4; // We want at least 4 MB
        private static final int MAX_SIZE;

        static {
            final long maxMemory = AppUtils.isHoneycombPlus() ? Runtime.getRuntime().maxMemory() : Debug.getNativeHeapSize();
            final long maxSizeLong = Math.max(MINIMAL_MAX_SIZE, (long) (maxMemory * DESIRED_PERCENTAGE_OF_MEMORY));
            // We limit it to a value as some implementations return Long.MAX_VALUE
            MAX_SIZE = (int) Math.min(((long) Integer.MAX_VALUE), Math.abs(maxSizeLong));
        }

        private final Observer cacheObserver = new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                if (observable instanceof BitmapRef) {
                    updateRef((BitmapRef) observable);
                }
            }
        };

        private void updateRef(BitmapRef ref) {
            final String uri = ref.getUri();
            put(uri, ref);
        }

        public BitmapRefCache() {
            super(MAX_SIZE);
        }

        @Override
        protected int sizeOf(String key, BitmapRef value) {
            if (value != null) {
                return value.getCurrentSize();
            }
            return 0;
        }

        @Override
        protected int previousSizeOf(String key, BitmapRef value) {
            if (value != null) {
                return value.getPreviousSize();
            }
            return 0;
        }

        @Override
        protected void entryRemoved(boolean evicted, String key, BitmapRef oldValue, BitmapRef newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
            if (oldValue != null && !oldValue.equals(newValue)) {
                // We now just recycle the ref by removing observers and nulling the bitmap ref
                oldValue.recycle();
            }
        }

        public void putAndObserve(String urlFrom, BitmapRef ref) {
            if (urlFrom == null || ref == null) {
                return;
            }
            put(urlFrom, ref);
            ref.setStickyObserver(cacheObserver);
        }
    }
}

