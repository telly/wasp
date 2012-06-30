package com.codeslap.wasp;

import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.Observable;

/**
 * Observer used to set the bitmap in given ImageView
 *
 * @author evelio
 * @version 1.0
 */
public class BitmapObserver implements UrlHolder {
    private String url;
    private final WeakReference<ImageView> viewRef;
    private final Handler uiHandler;

    /**
     * Creates an observer by associating a given imgView with given URL
     *
     * @param imgView         View to assign bitmap to
     * @param url             URL to associate
     * @param uiThreadHandler Handler created in UI Thread
     */
    public BitmapObserver(ImageView imgView, String url, Handler uiThreadHandler) {
        viewRef = new WeakReference<ImageView>(imgView);
        uiHandler = uiThreadHandler;
        setUrl(url);
    }

    /**
     * @param url url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof BitmapHelper.BitmapRef) {
            final BitmapHelper.BitmapRef ref = (BitmapHelper.BitmapRef) o;
            final String refUri = ref.getUri();
            if (refUri != null && refUri.equals(url)) {
                final Bitmap bmp = ref.getBitmap();
                final ImageView actualView = viewRef.get();
                if (actualView != null && BitmapUtils.isBitmapValid(bmp)) { //Check 1
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // in order to avoid repeating thumbnails or setting wrong ones, we check here
                            // the last tag (url) that was set to the image viewRef. that way, this we make
                            // sure the bitmap that is shown is the correct one
                            if (refUri.equals(actualView.getTag())) {
                                actualView.setImageBitmap(bmp);
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public String getUrl() {
        return url;
    }
}
