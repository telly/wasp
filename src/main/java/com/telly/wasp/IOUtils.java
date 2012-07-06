package com.telly.wasp;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * IO useful methods
 *
 * @author evelio
 * @version 1.0
 */
class IOUtils {
    private static boolean alreadyCheckedInternetPermission = false;

    /**
     * Non instance constants class
     */
    private IOUtils() {
    }

    /**
     * Read it's name
     */
    private static final int DEFAULT_BUFFER_SIZE = 4096; // 4 KiB

    /**
     * Copies an input stream to an output stream
     *
     * @param input  The source
     * @param output The target
     * @throws java.io.IOException From http://stackoverflow.com/questions/4064211
     */
    private static void copy(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int n;
        while ((n = input.read(buffer)) > 0) {// > 0 due zero sized streams
            output.write(buffer, 0, n);
        }
    }

    /**
     * Finds out the cache directory
     *
     * @param context Context to use
     * @return A File where means a directory where cache files should be written
     */
    public static File getCacheDirectory(Context context) {
        File cacheDir = context.getCacheDir();
        if (!cacheDir.exists() && cacheDir.mkdirs()) {
            Log.d(IOUtils.class.getSimpleName(), "Cache directory created");
        }
        return cacheDir;
    }

    /**
     * Download a file at <code>fromUrl</code> to a file specified by <code>toFile</code>
     *
     * @param fromUrl An url pointing to a file to download
     * @param toFile  File to save to, if existent will be overwrite
     * @throws java.io.IOException If fromUrl is invalid or there is any IO issue.
     */
    public static void downloadFile(Context context, String fromUrl, File toFile) throws IOException {
        downloadFileHandleRedirect(context, fromUrl, toFile, 0);
    }

    /**
     * Amount of maximum allowed redirects
     * number by:
     * http://www.google.com/support/forum/p/Webmasters/thread?tid=3760b68fb305088a&hl=en
     */
    private static final int MAX_REDIRECTS = 5;

    /**
     * Internal version of {@link #downloadFile(Context, String, java.io.File)}
     *
     * @param fromUrl  the url to download from
     * @param toFile   the file to download to
     * @param redirect true if it should accept redirects
     * @throws java.io.IOException
     */
    private static void downloadFileHandleRedirect(Context context, String fromUrl, File toFile, int redirect) throws IOException {
        if (context == null) {
            throw new RuntimeException("Context shall not be null");
        }
        if (!alreadyCheckedInternetPermission) {
            try {
                PackageManager pm = context.getPackageManager();
                PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
                String[] requestedPermissions = packageInfo.requestedPermissions;
                if (requestedPermissions == null) {
                    throw new RuntimeException("You must add android.permission.INTERNET to your app");
                }
                boolean found = false;
                for (String requestedPermission : requestedPermissions) {
                    if ("android.permission.INTERNET".equals(requestedPermission)) {
                        found = true;
                    }
                }
                if (!found) {
                    throw new RuntimeException("You must add android.permission.INTERNET to your app");
                } else {
                    alreadyCheckedInternetPermission = true;
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        if (redirect > MAX_REDIRECTS) {
            throw new IOException("Too many redirects for " + fromUrl);
        }

        URL url = new URL(fromUrl);
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        int contentLength = urlConnection.getContentLength();
        if (contentLength == -1) {
            fromUrl = urlConnection.getHeaderField("Location");
            if (fromUrl == null) { /* I'd love to leave it as "Que Dios se apiade de nosotros" XD */
                throw new IOException("No content or redirect found for URL " + url + " with " + redirect + " redirects.");
            }
            downloadFileHandleRedirect(context, fromUrl, toFile, redirect + 1);
            return;
        }
        InputStream input = urlConnection.getInputStream();
        OutputStream output = new FileOutputStream(toFile);
        IOUtils.copy(input, output);
        output.close();
        input.close();
    }
}