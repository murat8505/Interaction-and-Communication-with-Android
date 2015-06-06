package com.example.joeroger.homework2.utils;

import android.app.AlarmManager;
import android.content.Context;
import android.util.Log;

import com.example.joeroger.homework2.BuildConfig;
import com.example.joeroger.homework2.receiver.AlarmReceiver;


public class AlarmUtils {
    private static final String TAG = "AlarmUtils";

    public static void scheduleRecurringAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long delay = PreferenceUtils.getRefreshFrequencyMilli(context);

        if (delay <= 0) {
            Log.d(TAG, "User does not request updates");
            alarmManager.cancel(AlarmReceiver.buildPendingIntent(context));
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Scheduling alarm delay: " + delay);
        }
        // Prior to KITKAT, only had certain options: 15 min, 30 min, 1 hour, 12 hours, 24 hours
        // If you use any other interval it falls back to exact repeating which isn't as battery
        // friendly. So if you are trying to play nice, you should use
        // one of those time ranges.
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + delay,
                delay,
                AlarmReceiver.buildPendingIntent(context));
    }
}
