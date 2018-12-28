package com.mgstudio.imitationnettyclient.Utils;

import android.util.Log;

import com.mgstudio.imitationnettyclient.BuildConfig;

import timber.log.Timber;

public class DebugUtils {

    public static boolean debug_perssion = true;

    public static void debugLog(String tag, String msg) {
        if (BuildConfig.LOG_DEBUG)
            Log.e(Appfield.author+"/" + tag, msg);
    }

    public static void debugLog(String tag, String title, Object msg) {
        if (BuildConfig.LOG_DEBUG)
            Log.e(tag, title + "---------------------->" + msg);
    }

    public static void debugLog_Timber(String tag, String msg) {
        if (BuildConfig.LOG_DEBUG) Timber.e(msg);
    }
}
