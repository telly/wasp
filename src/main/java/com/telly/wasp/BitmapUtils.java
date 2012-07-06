package com.telly.wasp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author evelio
 * @version 1.0
 */
public class BitmapUtils {
    private static final BitmapFactory.Options normalOptions = new BitmapFactory.Options();

    static {
        normalOptions.inDither = true;
        normalOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        normalOptions.inPurgeable = true;
        normalOptions.inScaled = true;
    }

    public static Bitmap loadBitmapFile(String path) {
        try {
            return BitmapFactory.decodeFile(path, normalOptions);
        } catch (OutOfMemoryError error) {
            return null;
        }
    }

    public static int getBitmapSize(Bitmap bitmap) {
        return bitmap == null || bitmap.isRecycled() ? 0 : bitmap.getRowBytes() * bitmap.getHeight();
    }

    public static boolean isBitmapValid(Bitmap bmp) {
        return bmp != null && !bmp.isRecycled();
    }
}
