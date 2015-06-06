package com.example.joeroger.homework2.utils;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.example.joeroger.homework2.BuildConfig;
import com.example.joeroger.homework2.service.WeatherJobService;

public class JobUtils {

    private static final String TAG = JobUtils.class.getSimpleName();
    private static final int JOB_ID = 101;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleJob(Context context) {

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        long delay = PreferenceUtils.getRefreshFrequencyMilli(context);

        if (delay <= 0) {
            Log.d(TAG, "User does not request updates");
            jobScheduler.cancel(JOB_ID);
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Scheduling job delay: " + delay);
        }

        // Safe to reschedule jobs... New job replaces old one.
        JobInfo newJob = buildJobInfo(context, delay);
        int result = jobScheduler.schedule(newJob);
        if (result == JobScheduler.RESULT_FAILURE) {
            Log.w(TAG, "Job failed to be scheduled");
        }
        else {
            Log.d(TAG, "Job scheduled");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static JobInfo buildJobInfo(Context context, long interval) {


        return new JobInfo.Builder(JOB_ID, new ComponentName(context, WeatherJobService.class))

                // Want to run periodically. Other options are for one time runs, etc
                .setPeriodic(interval)

                        // Indicate want job to continue to run across a reboot of device. Requires the
                        // RECEIVE_BOOT_COMPLETED permission
                .setPersisted(true)

                        // Only want job to run if have un metered network access. Unmetered is suitable for large
                        // downloads without incurring cost to user. This does not mean it is Wifi. Therefore if
                        // you want to restrict to wifi, you likely have to do an additional check on job run.
                        // http://developer.android.com/reference/android/net/ConnectivityManager.html#isActiveNetworkMetered()
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)

                        // Indicate if you only want to be woken up if the device is charging.
                .setRequiresCharging(false)
                .build();
    }
}
