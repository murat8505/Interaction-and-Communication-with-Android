package com.example.joeroger.samplemaps.network;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.example.joeroger.samplemaps.service.NetworkAsyncJobService;
import com.example.joeroger.samplemaps.service.NetworkHandlerJobService;


public class JobUtils {

    private static final String TAG = JobUtils.class.getSimpleName();
    private static final int ASYNC_JOB_ID = 100;
    private static final int HANDLER_JOB_ID = 101;
    private static final long FOUR_MINUTES = 4 * 60 * 1000;
    private static final long TWO_HOURS = 2 * 60 * 60 * 1000;
    private static final long JOB_INTERVAL = TWO_HOURS;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleJob(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        // TODO No need to run both jobs. This is strictly for demo purposes.

        // Safe to reschedule jobs... New job replaces old one.
       /* JobInfo newJob = buildAsyncJobInfo(context);
        int result = jobScheduler.schedule(newJob);
        if (result == JobScheduler.RESULT_FAILURE) {
            Log.w(TAG, "Async Job failed to be scheduled");
        }
        else {
            Log.d(TAG, "Async Job scheduled");
        }*/

        JobInfo newJob = buildHandlerJobInfo(context);
        int result = jobScheduler.schedule(newJob);
        if (result == JobScheduler.RESULT_FAILURE) {
            Log.w(TAG, "Handler Job failed to be scheduled");
        }
        else {
            Log.d(TAG, "Handler Job scheduled");
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static JobInfo buildAsyncJobInfo(Context context) {
        return new JobInfo.Builder(ASYNC_JOB_ID, new ComponentName(context, NetworkAsyncJobService.class))

                // Want to run periodically. Other options are for one time runs, etc
                .setPeriodic(JOB_INTERVAL)

                        // Indicate want job to continue to run across a reboot of device. Requires the
                        // RECEIVE_BOOT_COMPLETED permission
                .setPersisted(true)

                        // Only backing off if get HTTP_ACCEPTED response indicating app should retry fetch
                        // otherwise, wait for next scheduled event.
                .setBackoffCriteria(500, JobInfo.BACKOFF_POLICY_EXPONENTIAL)

                        // Only want job to run if have un metered network access. Unmetered is suitable for large
                        // downloads without incurring cost to user. This does not mean it is Wifi. Therefore if
                        // you want to restrict to wifi, you likely have to do an additional check on job run.
                        // http://developer.android.com/reference/android/net/ConnectivityManager.html#isActiveNetworkMetered()
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)

                        // Indicate if you only want to be woken up if the device is charging.
                .setRequiresCharging(false)

                        // Advanced apps would add settings for interval and WIFI only... to allow the user
                        // to control how often this is done and under what conditions.
                .build();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static JobInfo buildHandlerJobInfo(Context context) {
        return new JobInfo.Builder(HANDLER_JOB_ID, new ComponentName(context, NetworkHandlerJobService.class))

                // Want to run periodically. Other options are for one time runs, etc
                .setPeriodic(JOB_INTERVAL)

                        // Indicate want job to continue to run across a reboot of device. Requires the
                        // RECEIVE_BOOT_COMPLETED permission
                .setPersisted(true)

                        // Only backing off if get HTTP_ACCEPTED response indicating app should retry fetch
                        // otherwise, wait for next scheduled event.
                .setBackoffCriteria(500, JobInfo.BACKOFF_POLICY_EXPONENTIAL)

                        // Only want job to run if have un metered network access. Unmetered is suitable for large
                        // downloads without incurring cost to user. This does not mean it is Wifi. Therefore if
                        // you want to restrict to wifi, you likely have to do an additional check on job run.
                        // http://developer.android.com/reference/android/net/ConnectivityManager.html#isActiveNetworkMetered()
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)

                        // Indicate if you only want to be woken up if the device is charging.
                .setRequiresCharging(false)

                        // Advanced apps would add settings for interval and WIFI only... to allow the user
                        // to control how often this is done and under what conditions.
                .build();
    }
}
