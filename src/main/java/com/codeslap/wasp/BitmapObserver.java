package com.codeslap.wasp;

import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Observer used to set the bitmap in given ImageView
 *
 * @author evelio
 * @version 1.0
 */
public class BitmapObserver extends BaseBitmapObserver {
    private final WeakReference<ImageView> viewRef;

    /**
     * Creates an observer by associating a given imgView with given URL
     *
     * @param imgView         View to assign bitmap to
     * @param url             URL to associate
     * @param uiThreadHandler Handler created in UI Thread
     */
    public BitmapObserver(ImageView imgView, String url, Handler uiThreadHandler) {
        super(url, uiThreadHandler);
        viewRef = new WeakReference<ImageView>(imgView);
    }

    @Override
    protected void doLoad(BitmapHelper.BitmapRef ref, final Bitmap bitmap) {
        final ImageView actualView = viewRef.get();
        if (actualView == null || !BitmapUtils.isBitmapValid(bitmap)) {
            return;
        }
        final String refUri = ref.getUri();
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                // in order to avoid repeating thumbnails or setting wrong ones, we check here
                // the last tag (url) that was set to the image viewRef. that way, this we make
                // sure the bitmap that is shown is the correct one
                if (refUri.equals(actualView.getTag())) {
                    actualView.setImageBitmap(bitmap);
                }
            }
        });
    }
}
