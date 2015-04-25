package com.example.joeroger.sampleplaces.bitmap;

import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * A simple bitmap cache based on the LruCache. A better cache would
 * recycle bitmap space.
 *
 * @author jrogers
 */
public class BitmapCache implements ComponentCallbacks2 {


    private static int cacheSize = 2 * 1024 * 1024; // 2MB assuming a 16MB device max
    private static BitmapCache cache = null;

    private final LruCache<String, Bitmap> lruCache;

    /**
     * Retrieve the bitmap cache singleton. This is not thread safe and
     * should be called only on the UI thread.
     *
     * @return The bitmap cache singleton
     */
    public static BitmapCache getSingleton(Context context) {
        if (cache == null) {
            cacheSize = computeCacheSizeBasedMemoryClass(context);
            cache = new BitmapCache(context);
        }
        return cache;
    }

    /**
     * Retrieve the cache when it should already exist. This is not thread safe and
     * should be called only on the UI thread.
     * @return
     * @throws java.lang.IllegalStateException if cache has not yet been created.
     */
    public static BitmapCache getSingleton() {
        if (cache == null) {
            throw new IllegalStateException("Cache not initialized");
        }
        return cache;
    }

    private BitmapCache(Context context) {

        context.getApplicationContext().registerComponentCallbacks(this);
        lruCache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }


    /**
     * Compute the cache size based on the memory class of the device
     *
     * @param context The context to use to lookup the memory class
     */
    public static int computeCacheSizeBasedMemoryClass(Context context) {
        // Get memory class of this device, exceeding this amount will throw an
        // OutOfMemory exception.
        final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

        // Use 1/8th of the available memory for this memory cache.
        final int newSize = 1024 * 1024 * memClass / 8;
        return newSize;
    }

    /**
     * Retrieve a bitmap from the cache for a given key
     *
     * @param key The string used to store the bitmap in the cache
     * @return The bitmap associated with the key, or null if the cache does not
     * contain a bitmap associated with the key.
     */
    public Bitmap getBitmap(String key) {
        return lruCache.get(key);
    }

    /**
     * Store a bitmap in the cache with an default expiration time of up to one day.
     *
     * @param key    The string to use to reference the bitmap in the cache
     * @param bitmap The bitmap being stored in the cache
     * @see #putBitmap(String, Bitmap)
     */
    public void putBitmap(String key, Bitmap bitmap) {
        lruCache.put(key, bitmap);
    }

    @Override
    public void onLowMemory() {
        lruCache.evictAll();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Do nothing
    }

    @Override
    public void onTrimMemory(int level) {
        // ANY CHANGES should be reflected in standard version
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
                // If running but OS is moderately low on memory trim by one quarter
                lruCache.trimToSize(lruCache.maxSize() - lruCache.maxSize() / 4);
                break;
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
                // If UI was hidden, or app is running but OS is low on memory trim by half
                lruCache.trimToSize(lruCache.maxSize() / 2);
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                // If app is running but OS is running critically low on memory
                // trim to 1/3
                lruCache.trimToSize(lruCache.maxSize() / 3);
                break;
            default:
                // Purge the entire cache in all other situations
                lruCache.evictAll();
        }
    }

    /**
     * Remove all bitmaps from the cache
     */
    public void clear() {
        lruCache.evictAll();
    }
}