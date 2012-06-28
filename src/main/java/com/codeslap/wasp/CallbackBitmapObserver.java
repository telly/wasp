package com.codeslap.wasp;

import android.graphics.Bitmap;
import android.os.Handler;

import java.util.Observable;
import java.util.Observer;

/**
 * Pretty much the same as{@link BitmapObserver} but this is designed to allow any implementation
 * of {@link BitmapCallback} to be notified on Bitmap load
 */
public class CallbackBitmapObserver implements Observer {
    private String url;
    private final BitmapCallback callbackRef;
    private final Handler uiHandler;

    /**
     * Creates an observer by associating a given callback with given URL
     *
     * @param callback        callback to call when Bitmap is ready
     * @param url             URL to associate
     * @param uiThreadHandler Handler created in UI Thread to call back
     */
    public CallbackBitmapObserver(BitmapCallback callback, String url, Handler uiThreadHandler) {
        callbackRef = callback;
        uiHandler = uiThreadHandler;
        setUrl(url);
    }

    /**
     * @param url url to set
     */
    public synchronized void setUrl(String url) {
        this.url = url;
    }

    public synchronized String getUrl() {
        return url;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof BitmapHelper.BitmapRef) {
            final BitmapHelper.BitmapRef ref = (BitmapHelper.BitmapRef) observable;
            final String refUri = ref.getUri();
            if (refUri != null && refUri.equals(url)) {
                final Bitmap bmp = ref.getBitmap();
                final BitmapCallback callback = callbackRef;
                if (callback != null && BitmapUtils.isBitmapValid(bmp) && callback.stillNeedsUrl(refUri)) { //Check 1
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // in order to avoid repeating thumbnails or setting wrong ones, we check here
                            // the last tag (url) that was set to the image viewRef. that way, this we make
                            // sure the bitmap that is shown is the correct one
                            if (callback.stillNeedsUrl(refUri)) {
                                callback.receiveBitmap(bmp);
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * Callback to be notified on bitmap load
     */
    public static interface BitmapCallback {
        /**
         * Indicate if this callback should be notified by calling {@link #receiveBitmap(android.graphics.Bitmap)}
         * when the bitmap is ready
         *
         * @param url Uniform Resource Locator pointing to a bitmap (PNG, JPG, etc.)
         * @return true if this callback needs the bitmap at a given URL
         *         false otherwise
         */
        boolean stillNeedsUrl(String url);

        /**
         * Called on UI thread when a bitmap is loaded
         *
         * @param bitmap the loaded bitmap
         */
        void receiveBitmap(Bitmap bitmap);
    }
}
