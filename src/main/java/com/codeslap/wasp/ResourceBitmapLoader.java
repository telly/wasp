package com.codeslap.wasp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author cristian
 * @version 1.0
 */
public class ResourceBitmapLoader implements BitmapLoader {
    private final Resources mResources;
    private final int mResId;

    public ResourceBitmapLoader(Resources resources, int resId) {
        mResources = resources;
        mResId = resId;
    }

    @Override
    public void load(Context context, String uri, File file) {
        Bitmap bitmap = BitmapFactory.decodeResource(mResources, mResId);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
