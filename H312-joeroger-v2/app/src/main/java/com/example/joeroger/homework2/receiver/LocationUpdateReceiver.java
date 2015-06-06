package com.example.joeroger.homework2.receiver;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.example.joeroger.homework2.BuildConfig;
import com.example.joeroger.homework2.service.LocationUpdateService;
import com.google.android.gms.location.FusedLocationProviderApi;

/**
 * Broadcast receiver for handling background location updates. Forwards to
 * the UpdateLocationService for processing.
 */
public class LocationUpdateReceiver extends WakefulBroadcastReceiver {

    private static final String ACTION_LOCATION_UPDATE = BuildConfig.APPLICATION_ID + ".receiver.action.locationUpdate";
    private static final int REQUEST = 800;

    public static PendingIntent buildPendingIntent(Context context) {
        Intent intent = new Intent(context, LocationUpdateReceiver.class);
        intent.setAction(ACTION_LOCATION_UPDATE);
        return PendingIntent.getBroadcast(context, REQUEST, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
        if (location != null) {
            startWakefulService(context, LocationUpdateService.buildBackgroundIntent(context, location));
        }
    }
}
