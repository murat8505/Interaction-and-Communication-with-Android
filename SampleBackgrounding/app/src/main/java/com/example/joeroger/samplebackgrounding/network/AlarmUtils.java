package com.example.joeroger.samplebackgrounding.network;

import android.app.AlarmManager;
import android.content.Context;
import android.util.Log;

import com.example.joeroger.samplebackgrounding.receiver.AlarmBroadcastReceiver;


public class AlarmUtils {

    private static final String TAG = "AlarmUtils";

    private static final long TWO_HOURS = 2 * 60 * 60 * 1000;
    private static final long FOUR_MINUTES = 4 * 60 * 1000;
    private static final long ALARM_DELAY = TWO_HOURS;
    private static final long FIFTEEN_MINUTES = 15 * 60 * 1000;

    public static void scheduleRecurringAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Log.d(TAG, "Scheduling alarm");
        // Prior to KITKAT, only had certain options: 15 min, 30 min, 1 hour, 12 hours, 24 hours
        // If you use any other interval it falls back to exact repeating which isn't as battery
        // friendly. So if you are trying to play nice, you should use
        // one of those time ranges.
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + ALARM_DELAY,
                ALARM_DELAY,
                AlarmBroadcastReceiver.buildPendingIntent(context));
    }

    public static void scheduleOneTimeAlarm(Context context, long backoff) {
        if (backoff > FIFTEEN_MINUTES) {
            return;
        }

        Log.d(TAG, "Scheduling backoff alarm");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long newBackoff = Math.round(backoff * 1.5d);
        long delay = System.currentTimeMillis() + backoff;
        alarmManager.set(AlarmManager.RTC_WAKEUP, delay, AlarmBroadcastReceiver.buildPendingIntent(context, newBackoff));
    }
}
