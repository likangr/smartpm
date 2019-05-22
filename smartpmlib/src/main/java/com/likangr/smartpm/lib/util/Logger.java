package com.likangr.smartpm.lib.util;

import android.util.Log;

import com.likangr.smartpm.lib.BuildConfig;

/**
 * @author likangren
 */
public class Logger {

    /**
     *
     */
    public static boolean DEBUG = BuildConfig.DEBUG;

    /**
     * @param tag
     * @param message
     */
    public static void v(String tag, String message) {
        if (DEBUG) {
            Log.v(tag, message);
        }
    }

    /**
     * @param tag
     * @param message
     */
    public static void d(String tag, String message) {
        if (DEBUG) {
            Log.d(tag, message);
        }
    }

    /**
     * @param tag
     * @param message
     */
    public static void i(String tag, String message) {
        if (DEBUG) {
            Log.i(tag, message);
        }
    }

    /**
     * @param tag
     * @param message
     */
    public static void w(String tag, String message) {
        if (DEBUG) {
            Log.w(tag, message);
        }
    }

    /**
     * @param tag
     * @param message
     */
    public static void e(String tag, String message) {
        if (DEBUG) {
            Log.e(tag, message);
        }
    }

    /**
     * @param tag
     * @param message
     * @param tr
     */
    public static void e(String tag, String message, Throwable tr) {
        if (DEBUG) {
            Log.e(tag, message, tr);
        }
    }
}