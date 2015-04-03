package com.example.joeroger.samplesearchshare.utils;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.example.joeroger.samplesearchshare.R;
import com.example.joeroger.samplesearchshare.activity.TopLevelActivity;
import com.example.joeroger.samplesearchshare.receiver.NotificationDeletedReceiver;

/**
 * Created by jrogers on 2/7/15.
 */
public class NotificationHelper {

    private static final String KEY_NOTIFICATION_COUNT = "notification_count";
    private static final int NOTIFICATION_ID = 100;

    public static void createNotification(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("notification", Context.MODE_PRIVATE);
        int notificationCount = preferences.getInt(KEY_NOTIFICATION_COUNT, 0) + 1;
        preferences.edit().putInt(KEY_NOTIFICATION_COUNT, notificationCount).apply();

        Notification publicNotification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_action_android)
                .setContentTitle("Homework 5")
                .setContentText(context.getResources().getQuantityString(R.plurals.notification_count, notificationCount, notificationCount))
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_action_android)
                .setContentTitle("Homework 5")
                .setContentText(context.getString(R.string.notification_item, notificationCount))
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPublicVersion(publicNotification)
                .setAutoCancel(true)
                // A delete intent is processed if user "swipes away" the notification or
                // clears all notifications.
                .setDeleteIntent(NotificationDeletedReceiver.buildPendingIntent(context))
                .setStyle(notificationCount == 1 ?
                        buildBigText(context, notificationCount) :
                        buildInbox(context, notificationCount));

        if (notificationCount == 1) {
            builder.addAction(R.drawable.ic_stat_action_android,
                    "Open",
                    TopLevelActivity.buildNotificationIntent(context));
        }
        else if (notificationCount > 1 || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            builder.setContentIntent(TopLevelActivity.buildNotificationIntent(context));
        }

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
    }

    public static void clearNotification(Context context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
        SharedPreferences preferences = context.getSharedPreferences("notification", Context.MODE_PRIVATE);
        preferences.edit().putInt(KEY_NOTIFICATION_COUNT, 0).apply();
    }

    private static NotificationCompat.Style buildBigText(Context context, int notificationCount) {
        return new NotificationCompat.BigTextStyle()
                .setBigContentTitle("Homework 5")
                .bigText(context.getString(R.string.notification_item_big_content, notificationCount));
    }

    private static NotificationCompat.Style buildInbox(Context context, int notificationCount) {
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle()
                .setBigContentTitle("Homework 5");

        int min = notificationCount - 4 > 1 ? notificationCount - 4 : 1;
        for (int i = notificationCount; i >= min; --i) {
            style.addLine(context.getString(R.string.notification_item, i));
        }

        return style;
    }
}
