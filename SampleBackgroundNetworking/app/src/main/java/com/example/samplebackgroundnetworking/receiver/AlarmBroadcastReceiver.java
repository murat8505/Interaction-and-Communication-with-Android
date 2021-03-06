package com.example.samplebackgroundnetworking.receiver;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.example.samplebackgroundnetworking.BuildConfig;
import com.example.samplebackgroundnetworking.network.AlarmUtils;
import com.example.samplebackgroundnetworking.network.ConnectivityUtils;
import com.example.samplebackgroundnetworking.service.NetworkIntentService;


public class AlarmBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = "AlarmBroadcastReceiver";

    private static final String ACTION_ALARM_FIRED = BuildConfig.APPLICATION_ID + ".receiver.action.ALARM_TRIGGERED";
    private static final String EXTRA_BACKOFF = BuildConfig.APPLICATION_ID + ".receiver.extra.backoff";

    public static PendingIntent buildPendingIntent(Context context) {
        return buildPendingIntent(context, 500);
    }

    public static PendingIntent buildPendingIntent(Context context, long backoff) {
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.setAction(ACTION_ALARM_FIRED);
        intent.putExtra(EXTRA_BACKOFF, backoff);
        return PendingIntent.getBroadcast(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, action);
        }

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
                AlarmUtils.scheduleRecurringAlarm(context);
                break;
            case ACTION_ALARM_FIRED:
                // Ensure connected to wifi and not metered...
                // Un-metered is not same as wifi, so to restrict to wifi only, you need to manually check. Also
                // it is possible the user marked the wifi as metered, so check that as well.
                if (ConnectivityUtils.isConnectedWifi(context) && !ConnectivityUtils.isConnectionMetered(context)) {
                    long backoff = intent.getLongExtra(EXTRA_BACKOFF, 500);
                    startWakefulService(context, NetworkIntentService.buildWakefulIntent(context, backoff));
                }
                else {
                    Log.d(TAG, "Not on wifi, skipping...");
                }
                break;
        }
    }
}
