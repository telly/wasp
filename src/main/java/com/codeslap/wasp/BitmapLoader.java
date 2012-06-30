package com.codeslap.wasp;

import android.content.Context;

import java.io.File;

/**
 * @author cristian
 */
public interface BitmapLoader {
    /**
     * Loads a bitmap and save it to the specified file
     *
     * @param context a context used to download/access the bitmap
     * @param uri     the uri or image identifier
     * @param file    the file to save the bitmap tol
     */
    void load(Context context, String uri, File file);
}
