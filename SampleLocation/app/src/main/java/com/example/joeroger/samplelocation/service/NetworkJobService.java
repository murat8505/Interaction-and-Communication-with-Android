package com.example.joeroger.samplelocation.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NetworkJobService extends JobService {

    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = 2;
    private static final int KEEP_ALIVE_TIME = 1;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "NetworkJobTask #" + mCount.getAndIncrement());
        }
    };

    // Shouldn't need more than 1, but just in case allow 10...
    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<>(10);

    // Setting up a private thread pool. By doing this, the Job Service will not interfere with
    // any other AsyncTask the application creates. It is a cheap way of reusing the AsyncTask
    // concepts without having to go all the way an actually manage the threads manually, yet
    // have a custom pool of threads.
    private static final Executor JOB_THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME,
            TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);

    private static final String TAG = "NetworkJobService";
    private LoadDataTask task;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "starting job");

        // Only run if on wifi... otherwise skip. This is a decision that needs to be
        // made for the data. Wifi is not a job filter so the app has to check manually.
        // This will prevent job from running on emulator, so you will need to remove.
        // Best option is add a "setting" allowing the user to choose...
        //if (ConnectivityUtils.isConnectedWifi(this)) {

            task = new LoadDataTask(this, params);
            task.executeOnExecutor(JOB_THREAD_POOL_EXECUTOR);
            return true;
       /* }
        else {
            Log.d(TAG, "Not on wifi, skipping...");
            return false;
        }*/
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "cancelling job");
        // Note, you should cancel out of your job as you will loose your wakelock upon
        // returning from this method.
        task.cancel(true);
        task = null;
        return false;
    }

    static class LoadDataTask extends AsyncTask<Void, Void, Void>
            implements FetchDataHelper.DelayAndRetryHandler {

        final Context applicationContext;
        final WeakReference<NetworkJobService> serviceRef;
        final JobParameters jobParameters;
        boolean retry = false;
        boolean error = false;

        LoadDataTask(NetworkJobService service, JobParameters jobParameters) {
            applicationContext = service.getApplicationContext();
            serviceRef = new WeakReference<>(service);
            this.jobParameters = jobParameters;
        }

        @Override
        protected Void doInBackground(Void... params) {

            FetchDataHelper.loadData(applicationContext, this);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            NetworkJobService service = serviceRef.get();
            if (service != null) {
                Log.d(TAG, "Job finished");
                service.jobFinished(jobParameters, retry);
            }
        }

        @Override
        public boolean isLoadCancelled() {
            return isCancelled();
        }

        @Override
        public void delayAndRetry() {
            retry = true;
        }

        @Override
        public void delayAndRetryError() {
            error = true;
        }
    }
}
