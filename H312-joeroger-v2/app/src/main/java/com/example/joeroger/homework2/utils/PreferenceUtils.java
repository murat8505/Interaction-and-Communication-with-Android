package com.example.joeroger.homework2.utils;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;


public class PreferenceUtils {

    private static final String TAG = "PreferenceUtils";

    public static final String REFRESH_FREQUENCY_KEY = "refresh_frequency";
    public static final String WIFI_ONLY_KEY = "wifi_only";

    public static String getPreference(Context context, String key) {
        return getPreference(context, key, "");
    }

    public static String getPreference(Context context, String key, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue);
    }

    public static long getRefreshFrequencyMilli(Context context) {
        String refreshFreq = getPreference(context, REFRESH_FREQUENCY_KEY, "60");

        try {
            return Long.parseLong(refreshFreq) * 60 * 1000;
        }
        catch (NumberFormatException e) {
            Log.w(TAG, "Failed to convert: " + refreshFreq);
            return -1;
        }
    }
}
