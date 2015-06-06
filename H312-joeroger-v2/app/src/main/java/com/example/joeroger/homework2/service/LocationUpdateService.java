package com.example.joeroger.homework2.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.example.joeroger.homework2.BuildConfig;
import com.example.joeroger.homework2.R;
import com.example.joeroger.homework2.utils.ConnectivityUtils;
import com.example.joeroger.homework2.utils.LocationUtils;
import com.example.joeroger.homework2.utils.PreferenceUtils;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;


/**
 * An intent service to update the current location in the background. The service is started
 * directly by the LocationService when a new location arrived, or via the background pending intents
 */
public class LocationUpdateService extends IntentService {

    private static final String TAG = "LocationUpdateService";

    private static final String EXTRA_LOCATION = "com.example.joeroger.homework2.service.extra.location";
    private static final String EXTRA_BACKGROUND = "com.example.joeroger.homework2.service.extra.background";

    public static Intent buildIntent(Context context, Location location) {
        Intent intent = new Intent(context, LocationUpdateService.class);
        intent.putExtra(EXTRA_LOCATION, location);
        return intent;
    }

    public static Intent buildBackgroundIntent(Context context, Location location) {
        Intent intent = new Intent(context, LocationUpdateService.class);
        intent.putExtra(EXTRA_LOCATION, location);
        intent.putExtra(EXTRA_BACKGROUND, true);
        return intent;
    }

    public LocationUpdateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            final boolean isBackground = intent.getBooleanExtra(EXTRA_BACKGROUND, false);
            final Location location = intent.getParcelableExtra(EXTRA_LOCATION);
            final Location lastLocation = LocationUtils.getLastLocation(this);


            if (BuildConfig.DEBUG) {
                Log.d(TAG, "checking new location: " + location);
            }

            if (LocationUtils.isBetterLocation(location, lastLocation)) {
                updateLocation(location, isBackground);

                // TODO remove temp notification.
                if (isBackground) {
                    Notification notification = new NotificationCompat.Builder(this)
                            .setContentTitle("New location")
                            .setContentText(location.toString())
                            .setSmallIcon(R.drawable.ic_action_content_add)
                            .setAutoCancel(true)
                            .build();
                    NotificationManagerCompat.from(this).notify(100, notification);
                }
            }
        }
        finally {
            WakefulBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    private void updateLocation(Location location, boolean isBackground) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Received new location: " + location);
        }

        // Wifi only matters if this is a background update.
        boolean wifiOnly = isBackground && PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceUtils.WIFI_ONLY_KEY, false);

        if (wifiOnly && !ConnectivityUtils.isConnectedWifi(this)) {
            Log.d(TAG, "User requests wifi only, and not on wifi. Skipping location update.");
            return;
        }

        final ServiceHelper serviceHelper = new ServiceHelper(this);

        LatLng adjustedLocation = adjustLocation(location);

        if (serviceHelper.fetchNewLocation(adjustedLocation)) {
            // Saving the user's exact position to avoid fetching location too much.
            Log.d(TAG, "Current city updated in database, saving location");
            LocationUtils.saveLastLocation(this, location);
            serviceHelper.purgeOldCities();
        }
    }

    /**
     * Use geocoder, to determine "true" city for user's current position to compensate for
     * in consistant results from weather.org.
     *
     * @param location The user's current position
     * @return The "adjusted" location of the user.
     */
    private LatLng adjustLocation(Location location) {
        // For any error, just return the user's current position
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (!Geocoder.isPresent()) {
            return currentLocation;
        }
        Geocoder geocoder = new Geocoder(this);

        try {
            List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (list.isEmpty()) return currentLocation;
            Address address = list.get(0);
            if (address.getLocality() != null && address.getAdminArea() != null) {
                // locality = city, admin area = state in the US. This likely will fail in other countries.
                list = geocoder.getFromLocationName(address.getLocality() + ", " + address.getAdminArea(), 1);
                if (list.isEmpty()) return currentLocation;
                address = list.get(0);
                // Weather.org is good if you give exact lat/long of a city, not for outskirts.
                // So return the "adjusted" location of the user
                return new LatLng(address.getLatitude(), address.getLongitude());
            }
        }
        catch (IOException e) {
            Log.w(TAG, "Unable to adjust location");
        }
        return currentLocation;
    }
}
