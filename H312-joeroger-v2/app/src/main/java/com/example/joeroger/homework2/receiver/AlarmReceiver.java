package com.example.joeroger.homework2.receiver;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.example.joeroger.homework2.BuildConfig;
import com.example.joeroger.homework2.service.WeatherService;
import com.example.joeroger.homework2.utils.AlarmUtils;
import com.example.joeroger.homework2.utils.ConnectivityUtils;
import com.example.joeroger.homework2.utils.PreferenceUtils;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private static final String ACTION_ALARM_FIRED = BuildConfig.APPLICATION_ID + ".receiver.action.ALARM_TRIGGERED";
    private static final String TAG = "AlarmReceiver";

    public static PendingIntent buildPendingIntent(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_ALARM_FIRED);
        return PendingIntent.getBroadcast(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, action);
        }

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
                // Ensure alarm scheduled across a boot.
                AlarmUtils.scheduleRecurringAlarm(context);
                break;
            case ACTION_ALARM_FIRED:

                // No connectivity, so quit
                if (!ConnectivityUtils.isConnected(context)) {
                    Log.d(TAG, "Not connected, skipping update...");
                    return;
                }

                // If user wants wifi only,
                boolean wifiOnly = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferenceUtils.WIFI_ONLY_KEY, false);

                if (wifiOnly && !ConnectivityUtils.isConnectedWifi(context)) {
                    Log.d(TAG, "User requests wifi only, and not on wifi. Skipping update...");
                    return;
                }

                startWakefulService(context, WeatherService.buildIntent(context));
                break;
        }
    }
}
