package com.example.joeroger.samplelocation.receiver;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.example.joeroger.samplelocation.utils.LocationUtils;
import com.google.android.gms.location.FusedLocationProviderApi;

public class LocationBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final int REQUEST_CODE = 100;

    public static PendingIntent buildPendingIntent(Context context) {
        Intent intent = new Intent(context, LocationBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);

        // Only saving location. In other apps. (AKA weather) the new location should be forwarded
        // to a wakeful intent service which verifies location is "better" and if so updates the
        // current city weather in the database.
        LocationUtils.saveLastLocation(context, location);
    }
}
