package com.example.joeroger.samplesearchshare.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.joeroger.samplesearchshare.utils.NotificationHelper;

public class NotificationDeletedReceiver extends BroadcastReceiver {

    public static PendingIntent buildPendingIntent(Context context) {
        Intent intent = new Intent(context, NotificationDeletedReceiver.class);
        return PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public NotificationDeletedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // If user deleted, treat it as a normal clear which resets the count...
        Log.d("NotifDeletedReceiver", "Notification deleted");

        // Limited amount of time to complete work. If have more work create a service
        // from here.
        NotificationHelper.clearNotification(context);
    }
}
