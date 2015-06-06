package com.example.joeroger.homework2.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.joeroger.homework2.utils.ConnectivityUtils;
import com.example.joeroger.homework2.utils.PreferenceUtils;
import com.google.android.gms.common.api.Status;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class WeatherJobService extends JobService
        implements ServiceHelper.CancellationCallback {

    private static final String TAG = "WeatherJobService";

    private Looper workLooper;
    private JobHandler handler;
    private boolean cancelled = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create the handler thread. Optionally you can control the priority of the thread. Since the job
        // is holding a wake lock, go ahead and be "DEFAULT" which runs slower than UI priority. The only
        // other real option is THREAD_PRIORITY_BACKGROUND which reduces the chance of interfering with the
        // UI thread.
        HandlerThread thread = new HandlerThread("WeatherJobService Thread", android.os.Process.THREAD_PRIORITY_DEFAULT);
        thread.start();
        workLooper = thread.getLooper();
        handler = new JobHandler(workLooper);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Starting job");

        boolean wifiOnly = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceUtils.WIFI_ONLY_KEY, false);

        if (wifiOnly && !ConnectivityUtils.isConnectedWifi(this)) {
            Log.d(TAG, "User requests wifi only, and not on wifi. Job finished.");
            return false;
        }

        // Move work to background thread...
        Message msg = handler.obtainMessage();
        msg.obj = params;
        handler.sendMessage(msg);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Cancelling job");
        cancelled = true;
        stopService(LocationService.buildIntent(this));
        return false;
    }

    @Override
    public void onDestroy() {
        // Signal the looper to quit the thread immediately.
        workLooper.quit();
        super.onDestroy();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    protected void doBackgroundWork() {
        final ServiceHelper helper = new ServiceHelper(this, this);
        helper.fetchData();
    }

    /* package */ class JobHandler extends Handler {


        public JobHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                // If had multiple message types, the "what" parameter should be set and then switch
                // on what do determine what to do. In this case, only one kind of message so, just
                // do the background work.
                doBackgroundWork();
            }
            catch (Exception e) {
                Log.w(TAG, "Unexpected Exception:", e);
            }
            finally {
                // Regardless of what happens, marking job as finished at this point with no retry
                Log.d(TAG, "Job finished");
                jobFinished((JobParameters) msg.obj, false);
                // For safety, clear the message obj to prevent leaks.
                msg.obj = null;
            }
        }
    }
}
