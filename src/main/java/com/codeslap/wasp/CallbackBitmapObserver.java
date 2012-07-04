package com.codeslap.wasp;

import android.graphics.Bitmap;
import android.os.Handler;

/**
 * Pretty much the same as{@link BitmapObserver} but this is designed to allow any implementation
 * of {@link BitmapCallback} to be notified on Bitmap load
 */
public class CallbackBitmapObserver extends BaseBitmapObserver {
    private final BitmapCallback mCallbackRef;

    /**
     * Creates an observer by associating a given callback with given URL
     *
     * @param callback        callback to call when Bitmap is ready
     * @param url             URL to associate
     * @param uiThreadHandler Handler created in UI Thread to call back
     */
    public CallbackBitmapObserver(BitmapCallback callback, String url, Handler uiThreadHandler) {
        super(url, uiThreadHandler);
        mCallbackRef = callback;
    }

    /**
     * Creates an observer by associating a given callback with given URL. This will
     * execute the callbacks in a non-UI thread. If you want to perform task that must
     * be run on the ui thread, send a {@link Handler} using the
     * {@link CallbackBitmapObserver#CallbackBitmapObserver(BitmapCallback, String, Handler)}
     * constructor.
     *
     * @param callback callback to call when Bitmap is ready
     * @param url      URL to associate
     */
    public CallbackBitmapObserver(BitmapCallback callback, String url) {
        super(url, null);
        mCallbackRef = callback;
    }

    @Override
    protected void doLoad(BitmapHelper.BitmapRef reference, final Bitmap bitmap) {
        final String refUri = reference.getUri();
        if (mCallbackRef == null || !BitmapUtils.isBitmapValid(bitmap) || !mCallbackRef.stillNeedsUrl(refUri)) {
            return;
        }
        Runnable runnableCallback = new Runnable() {
            @Override
            public void run() {
                // in order to avoid repeating thumbnails or setting wrong ones, we check here
                // the last tag (url) that was set to the image viewRef. that way, this we make
                // sure the bitmap that is shown is the correct one
                if (mCallbackRef.stillNeedsUrl(refUri)) {
                    mCallbackRef.receiveBitmap(refUri, bitmap);
                }
            }
        };
        Handler handler = getHandler();
        if (handler == null) { // if there is no handler, just run the callback
            runnableCallback.run();
        } else {
            handler.post(runnableCallback);
        }
    }

    /**
     * Callback to be notified on bitmap load
     */
    public static interface BitmapCallback {
        /**
         * Indicate if this callback should be notified by calling {@link #receiveBitmap(String, android.graphics.Bitmap)}
         * when the bitmap is ready
         *
         * @param uri Uniform Resource Identifier pointing to a bitmap (PNG, JPG, etc.)
         * @return true if this callback needs the bitmap at a given URL
         *         false otherwise
         */
        boolean stillNeedsUrl(String uri);

        /**
         * Called on UI thread when a bitmap is loaded
         *
         * @param uri    the uri for this bitmap
         * @param bitmap the loaded bitmap
         */
        void receiveBitmap(String uri, Bitmap bitmap);
    }

    /**
     * Callback to be notified on bitmap load
     */
    public static abstract class SimpleBitmapCallback implements BitmapCallback {
        @Override
        public final boolean stillNeedsUrl(String uri) {
            return true;
        }

        @Override
        public abstract void receiveBitmap(String uri, Bitmap bitmap);
    }
}
