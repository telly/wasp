package com.telly.wasp;

import android.os.Build;

/**
 * Collection of static methods related to app operations
 *
 * @author evelio
 * @version 1.0
 */
final class AppUtils {
    private static final int SDK_HONEYCOMB_API_LEVEL = 11;
    private static final int SDK_NINE_API_LEVEL = 9;

    /**
     * Determine if current SDK version/API Level is Honeycomb (11)
     *
     * @return true if is Honeycomb or later (ICS)
     *         false otherwise
     */
    public static boolean isHoneycombPlus() {
        return Build.VERSION.SDK_INT >= SDK_HONEYCOMB_API_LEVEL;
    }

    public static boolean isNinePlus() {
        return Build.VERSION.SDK_INT >= SDK_NINE_API_LEVEL;
    }
}
