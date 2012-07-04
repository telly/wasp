package com.codeslap.wasp;

import android.graphics.Bitmap;
import android.os.Handler;

import java.util.Observable;

/**
 * @author cristian
 * @version 1.0
 */
public abstract class BaseBitmapObserver implements UrlHolder {

    private String mUrl;
    private final Handler mHandler;
    private boolean mTakeUriIntoAccount = true;

    protected BaseBitmapObserver(String url, Handler uiThreadHandler) {
        mUrl = url;
        mHandler = uiThreadHandler;
    }

    /**
     * @param url url to set
     */
    public synchronized void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public synchronized String getUrl() {
        return mUrl;
    }

    public Handler getHandler() {
        return mHandler;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (!(observable instanceof BitmapHelper.BitmapRef)) {
            return;
        }
        final BitmapHelper.BitmapRef ref = (BitmapHelper.BitmapRef) observable;

        final String refUri = ref.getUri();
        if (!mTakeUriIntoAccount || (refUri != null && refUri.equals(mUrl))) {
            final Bitmap bitmap = ref.getBitmap();
            doLoad(ref, bitmap);
        }
    }

    public void setTakeUriIntoAccount(boolean takeUriIntoAccount) {
        mTakeUriIntoAccount = takeUriIntoAccount;
    }

    protected abstract void doLoad(BitmapHelper.BitmapRef ref, Bitmap bitmap);

    public static class NoOpObserver extends BaseBitmapObserver{
        public NoOpObserver() {
            super(null, null);
        }

        @Override
        protected void doLoad(BitmapHelper.BitmapRef ref, Bitmap bitmap) {
            // do nothing
        }
    }
}
