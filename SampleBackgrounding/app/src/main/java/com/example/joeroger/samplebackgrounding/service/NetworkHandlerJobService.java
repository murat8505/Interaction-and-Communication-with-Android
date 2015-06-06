
package com.example.joeroger.samplebackgrounding.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NetworkHandlerJobService extends JobService
        implements FetchDataHelper.DelayAndRetryHandler {

    private Looper workLooper;
    private JobHandler handler;
    private boolean cancelled = false;
    private boolean retry = false;
    private boolean error = false;

    private static final String TAG = "NetworkHandlerJobServ";

    @Override
    public void onCreate() {
        super.onCreate();

        // Create the handler thread. Optionally you can control the priority of the thread. Since the job
        // is holding a wake lock, go ahead and be "DEFAULT" which runs slower than UI priority. The only
        // other real option is THREAD_PRIORITY_BACKGROUND which reduces the chance of interfering with the
        // UI thread.
        HandlerThread thread = new HandlerThread("NetworkHandlerJobService Thread", Process.THREAD_PRIORITY_DEFAULT);
        thread.start();
        workLooper = thread.getLooper();
        handler = new JobHandler(workLooper);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "starting job");

        // TODO may want to check to see if on wifi if that is a requirement. Job filtering
        // TODO can only ensure the network is available, or available and not metered.

        Message msg = handler.obtainMessage();
        msg.obj = params;
        handler.sendMessage(msg);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "cancelling job");
        cancelled = true;
        return false;
    }

    @Override
    public void onDestroy() {
        // Signal the looper to quit the thread immediately.
        workLooper.quit();
        super.onDestroy();
    }

    @Override
    public boolean isLoadCancelled() {
        return cancelled;
    }

    @Override
    public void delayAndRetry() {
        retry = true;
    }

    @Override
    public void delayAndRetryError() {
        error = true;
    }

    protected void doBackgroundWork() {
        Log.d(TAG, Thread.currentThread().getName());
        FetchDataHelper.loadData(getApplicationContext(), this);
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
                // Regardless of what happens, marking job as finished at this point.
                Log.d(TAG, "Job Finished");
                jobFinished((JobParameters) msg.obj, retry);
            }
        }
    }
}
