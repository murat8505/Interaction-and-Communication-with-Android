/*
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.joeroger.sampleplaces.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.example.joeroger.sampleplaces.BuildConfig;
import com.example.joeroger.sampleplaces.R;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * AsyncTask designed to load a bitmap from an URL and place it in the specified view when complete.
 * Derives from HttpGetTask.  The class can be used as is with no changes.  However if you need custom
 * cache times, cache usage, etc you should override configureHttpRequest as documented in BaseHttpTask
 */
public class BitmapLoadTask extends AsyncTask<Void, Void, Bitmap> {

    private static final String TAG = "BitmapLoadTask";

    private static final int NO_RESOURCE_ID = 0;

    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = 2;
    private static final int KEEP_ALIVE_TIME = 1;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "BitmapTask #" + mCount.getAndIncrement());
        }
    };

    // Shouldn't need more than 1, but just in case allow 10...
    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<>(128);

    // Setting up a private thread pool. By doing this, the Job Service will not interfere with
    // any other AsyncTask the application creates. It is a cheap way of reusing the AsyncTask
    // concepts without having to go all the way an actually manage the threads manually, yet
    // have a custom pool of threads.
    public static final Executor BITMAP_THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME,
            TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);


    private final WeakReference<ImageView> viewRef;
    private final int errorResourceId;
    private URL url = null;

    /**
     * Constructor
     *
     * @param url  The url associated with the image to place in the view
     * @param view The view to place the bitmap in when complete
     */
    public BitmapLoadTask(String url, ImageView view) {
        this(url, view, NO_RESOURCE_ID);
    }

    public BitmapLoadTask(String url, ImageView view, int errorResourceId) {
        try {
            this.url = new URL(url);
        }
        catch (MalformedURLException e) {
            this.url = null;
        }

        viewRef = new WeakReference<>(view);
        this.errorResourceId = errorResourceId != NO_RESOURCE_ID ? errorResourceId : android.R.color.transparent;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // If image view is null, then pre-execute has nothing more to do
        ImageView view = getView();

        if (view == null) {
            Log.d(TAG, "onPreExecute: View is null");
            clearViewRefAndCancelTask();
            return;
        }

        // Check to see if this load, or an existing load needs to be canceled for the view
        cancelLoadForViewIfNecessary(view);

        if (url == null) {
            Log.d(TAG, "onPreExecute: Url is null");
            clearViewRefAndCancelTask();
            return;
        }

        // If something else cancelled this task, then just quit.
        if (isCancelled()) {
            return;
        }

        // If bitmap in image cache, set it in the view and cancel.
        Bitmap bm = BitmapCache.getSingleton(view.getContext()).getBitmap(url.toString());
        if (bm != null) {
            view.setTag(R.id.BitmapLoadTaskId, null);
            view.setImageBitmap(bm);
            this.cancel(true);
            return;
        }

        // We are going to load data in another thread, so track ourselves.
        // Use weak reference so the task goes out of scope when complete.
        view.setImageResource(android.R.color.transparent);
        view.setTag(R.id.BitmapLoadTaskId, new WeakReference<>(this));
    }

    @Override
    protected Bitmap doInBackground(Void... params) {

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            // Using 15 sec timeouts.
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);
            urlConnection.connect();

            int status = urlConnection.getResponseCode();

            // For Ok, process result
            if (status == HttpURLConnection.HTTP_OK) {
                return createBitmap(urlConnection.getInputStream());
            }

            Log.w(TAG, "status: " + status);

            // Only logging contents. Depending on the API, there may be other info that is
            // useful to the application, developer, or possibly the user. For example if
            // you change the url and misspell "event" in the column name it indicates that
            // the column was not found.
            logErrorStream(urlConnection.getErrorStream());
            return null;
        }
        catch (IOException ex) {
            Log.e(TAG, "Failed to load data: " + ex.getMessage());
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "exception", ex);
            }
            return null;
        }
        finally {
            // Return connection back to the pool or to be closed.
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);

        // Place the bitmap in the memory cache even if no view if have a valid result
        if (result != null) {
            BitmapCache.getSingleton().putBitmap(url.toString(), result);
        }

        ImageView view = getView();
        // If view is null, then it was garbage collected and we have nothing to do
        if (view == null) {
            Log.d(TAG, "onPostExecute: View is null");
            return;
        }

        // Clear tag since we can not be canceled any more
        view.setTag(R.id.BitmapLoadTaskId, null);

        // If no result, output error and set to error image if provided.
        if (result == null) {
            view.setImageResource(errorResourceId);
            return;
        }

        // Success so set bitmap in view.
        view.setImageBitmap(result);
    }

    private Bitmap createBitmap(InputStream stream) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(stream, 4096);
        try {
            return BitmapFactory.decodeStream(stream, null, null);
        }
        finally {
            bufferedInputStream.close();
        }
    }

    /**
     * Provide access to the view to derived classes.
     * Note: this will return null if the view has been garbage collected
     *
     * @return The image view used to create this task
     */
    @Nullable
    protected ImageView getView() {
        return viewRef.get();
    }

    /**
     * Helper for derived classes to allow clearing of the viewRef
     */
    public final void clearViewRefAndCancelTask() {
        viewRef.clear();
        cancel(true);
    }

    /**
     * Helper to retrieve the current bitmap task associated with the view.
     *
     * @return The current task, or null if no task exists
     */
    public static BitmapLoadTask getCurrentBitmapTask(ImageView view) {

        @SuppressWarnings("unchecked")
        WeakReference<BitmapLoadTask> currentLoaderRef = (WeakReference<BitmapLoadTask>) view.getTag(R.id.BitmapLoadTaskId);

        return currentLoaderRef != null ? currentLoaderRef.get() : null;
    }

    /**
     * Helper method that checks to see if an existing load is ongoing for the view.  This is most likely if the image
     * view is in an AdapterView of some sort such as a ListView
     *
     * @param view The image view to check
     */
    private void cancelLoadForViewIfNecessary(ImageView view) {
        BitmapLoadTask currentLoader = getCurrentBitmapTask(view);

        // If a loader exists, then see which task needs to be cancelled.
        if (currentLoader != null) {
            // Same view, so do nothing.  Should be impossible.
            if (currentLoader == this) {
                return;
            }
            // If already loading this url for this view, cancel this new loader, since the older loader will
            // do the right thing faster
            else if (currentLoader.url.equals(url)) {
                clearViewRefAndCancelTask();
                return;
            }
            // Else loading a different url so cancel the old load task and clear its view ref,
            // to prevent it from populating the view.
            else {
                currentLoader.clearViewRefAndCancelTask();
            }
        }
    }

    private static void logErrorStream(@Nullable InputStream errorStream) throws IOException {
        if (errorStream == null) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"));
        try {
            String result;
            while ((result = reader.readLine()) != null) {
                builder.append(result);
            }
        }
        finally {
            reader.close();
        }

        Log.w(TAG, builder.toString());
    }
}
