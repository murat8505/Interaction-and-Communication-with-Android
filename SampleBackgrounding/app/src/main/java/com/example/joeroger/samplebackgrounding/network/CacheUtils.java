package com.example.joeroger.samplebackgrounding.network;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.net.http.HttpResponseCache;
import android.util.Log;

import com.example.joeroger.samplebackgrounding.BuildConfig;

import java.io.File;
import java.io.IOException;


public final class CacheUtils {

    private static final String TAG = CacheUtils.class.getSimpleName();

    /**
     * Setup the http response cache. By default http responses are not cached.
     */
    public static synchronized void initializeCache(Context context) {

        HttpResponseCache cache = HttpResponseCache.getInstalled();

        if (cache == null) {
            try {
                File httpCacheDir = new File(context.getCacheDir(), "http");
                long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
                HttpResponseCache.install(httpCacheDir, httpCacheSize);
                context.getApplicationContext().registerComponentCallbacks(new CacheCallbacks());
            }
            catch (IOException e) {
                Log.w(TAG, "HTTP response cache installation failed:" + e);
            }
        }
    }

    /**
     * Flushes all in-memory responses to disk to make available for next app run
     *
     */
    public static synchronized void flushCache() {
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Cache flushed");
            }
        }
    }

    public static void logCache() {
        if (BuildConfig.DEBUG) {
            HttpResponseCache cache = HttpResponseCache.getInstalled();
            if (cache != null) {
                Log.d(TAG, "Hit: " + cache.getHitCount() + ", Network: " + cache.getNetworkCount() + ", Request: " + cache.getRequestCount());
            }
        }
    }

    /* package */ static class CacheCallbacks implements ComponentCallbacks2 {

        @Override
        public void onTrimMemory(int level) {
            // Use trim memory levels, to "flush" the cache from memory to disk
            switch (level) {
                case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
                case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
                case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                    CacheUtils.flushCache();
                    break;

                // See ComponentCallbacks2 for complete list of constants and what they mean.
            }
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            // Do nothing.
        }

        @Override
        public void onLowMemory() {
            // Do nothing
        }
    }
}
