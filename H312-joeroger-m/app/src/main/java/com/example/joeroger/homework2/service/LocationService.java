package com.example.joeroger.homework2.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.joeroger.homework2.BuildConfig;
import com.example.joeroger.homework2.receiver.LocationSettingsReceiver;
import com.example.joeroger.homework2.receiver.LocationUpdateReceiver;
import com.example.joeroger.homework2.utils.LocationUtils;
import com.example.joeroger.homework2.utils.PreferenceUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;


public class LocationService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult>,
        Handler.Callback {

    private static final String TAG = "LocationService";

    private static final int GET_LAST_LOCATION = 101;

    private static final long THIRTY_SECONDS = 30 * 1000;
    private static final long TEN_SECONDS = 10 * 1000;
    private static final float MIN_DISTANCE = 1000; // 1 kilometers

    private IBinder binder = new LocationServiceBinder();
    private GoogleApiClient googleApiClient;
    private LocationCallback locationCallback;
    private Location lastLocation;
    private long backgroundLocationDelay = 60 * 60 * 1000;
    private LocationRequest locationRequest;
    private LocationRequest backgroundLocationRequest;
    private boolean resolveLocationSettings = true;
    private boolean foregroundUpdates = false;
    private Handler handler;


    public interface LocationServiceMgr {
        void onPlayServicesAreAvailable();
    }

    @NonNull
    public static Intent buildIntent(@NonNull Context context) {
        return new Intent(context, LocationService.class);
    }

    public class LocationServiceBinder extends Binder implements LocationServiceMgr {

        @Override
        public void onPlayServicesAreAvailable() {
            LocationService.this.onPlayServicesAreAvailable();
        }
    }

    public LocationService() {
        Log.d(TAG, "Constructed");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        HandlerThread handlerThread = new HandlerThread("LocationServiceThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper(), this);

        handler.sendMessage(handler.obtainMessage(GET_LAST_LOCATION));
        locationCallback = new MyLocationCallback();

        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // First built the request the fragment will be using to request updates.
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (BuildConfig.DEBUG && Build.BRAND.startsWith("generic")) {
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        locationRequest.setSmallestDisplacement(MIN_DISTANCE);

        // For this app we are only using location to filter 911 events in roughly 1 km rectangle
        // around your current location.

        // How frequent you want to be informed, the slower the better even if you want GPS.
        locationRequest.setInterval(THIRTY_SECONDS);

        // If another app requests faster updates, what is the max rate you want to be informed
        locationRequest.setFastestInterval(TEN_SECONDS);

        // Max time you are willing to wait for update, ideally double your interval. This lets
        // the fused location manager schedule your app with others to reduce battery use, etc
        locationRequest.setMaxWaitTime(2 * THIRTY_SECONDS);

        backgroundLocationRequest = LocationRequest.create();
        backgroundLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (BuildConfig.DEBUG && Build.BRAND.startsWith("generic")) {
            backgroundLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        backgroundLocationRequest.setSmallestDisplacement(MIN_DISTANCE);
    }

    @Override
    public IBinder onBind(Intent intent) {

        Log.d(TAG, "onBind()");
        googleApiClient.connect();
        resolveLocationSettings = true;
        handler.sendMessage(handler.obtainMessage(GET_LAST_LOCATION));
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (googleApiClient.isConnected()) {
            if (foregroundUpdates) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationCallback);
            }
            googleApiClient.disconnect();
        }
        return false;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationCallback);
        }
        googleApiClient.disconnect();
        googleApiClient.unregisterConnectionCallbacks(this);
        googleApiClient.unregisterConnectionFailedListener(this);
        googleApiClient = null;

        handler.getLooper().quit();
        handler = null;
        locationCallback = null;
        super.onDestroy();
    }

    public void onPlayServicesAreAvailable() {
        // Notified by the activity that play services are now available. If
        // not connected, or connecting, then restart the connection process.
        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Connected to Google Play services!
        // The good stuff goes here, if you are able to do things that are outside of UI
        // interaction. For places, it depends on search so this app has nothing to do.
        Log.d(TAG, "Connected");

        // Remove background updates while active.
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,
                LocationUpdateReceiver.buildPendingIntent(this));

        // Request location updates. Using new settings callback to determine if settings are
        // enabled or not.
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // Lost connection.
        // Good place to cleanup or suspect any background ops that depend on the connection.
        // Not sure what more you could do...
        // BTW the googleApiClient has isConnected()/isConnecting() methods so no need to track.
        Log.d(TAG, "Connection suspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.w(TAG, GooglePlayServicesUtil.getErrorString(connectionResult.getErrorCode()));
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                requestLocationUpdates();
                return;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. But could be fixed by showing the user
                // a dialog.
                LocationSettingsReceiver.sendLocationSettings(this, status);
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way to fix the
                // settings so we won't show the dialog...
                break;
        }

        // Must be in an error state, fallback to last known location
        requestLastKnownLocation();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case GET_LAST_LOCATION:
                lastLocation = LocationUtils.getLastLocation(this);
                backgroundLocationDelay = PreferenceUtils.getRefreshFrequencyMilli(this);
                return true;
            default:
                return false;
        }
    }

    private void requestLocationUpdates() {

        // First refresh using last known location so user doesn't wait for location request.
        requestLastKnownLocation();

        // Request updates using our location request criteria. If background location delay,
        // then rely on it, otherwise start a foreground request.

        if (backgroundLocationDelay > 0) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Requesting background location updates delay: " + backgroundLocationDelay);
            }
            backgroundLocationRequest.setInterval(backgroundLocationDelay);
            backgroundLocationRequest.setFastestInterval(backgroundLocationDelay >> 1); // 1/2 value
            PendingResult<Status> result = LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient,
                    backgroundLocationRequest,
                    LocationUpdateReceiver.buildPendingIntent(this));
            result.setResultCallback(new LocationStatusCallback());
        }

        Log.d(TAG, "Requesting foreground updates");
        PendingResult<Status> result = LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, locationCallback, null);

        result.setResultCallback(new LocationStatusCallback());
        foregroundUpdates = true;
    }

    private void requestLastKnownLocation() {
        Location newLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (LocationUtils.isBetterLocation(newLocation, lastLocation)) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Using last location: " + newLocation);
            }
            startService(LocationUpdateService.buildIntent(this, newLocation));
            lastLocation = newLocation;
        }
    }

    // Using new callback for location that was released on April 28. The advantage
    // is it provides both locations and a notification for location availability if you want to
    // handle it. Essentially all methods in the callback are optional so only override what the
    // app is interested in.
    /* package */ class MyLocationCallback extends LocationCallback {

        @Override
        public void onLocationResult(LocationResult result) {
            super.onLocationResult(result);
            Location location = result.getLastLocation();
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onLocationChanged: " + location);
            }
            if (LocationUtils.isBetterLocation(location, lastLocation)) {
                startService(LocationUpdateService.buildIntent(LocationService.this, location));
                lastLocation = location;
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onLocationAvailability: " + locationAvailability.isLocationAvailable());
            }
            // Only attempt to resolve location settings once...
            if (resolveLocationSettings && !locationAvailability.isLocationAvailable()) {
                // Ensure location service settings are enabled for what we need. If you have different
                // location requests, you may check both at same time.

                // The advantage of this callback, is can request locations right away. Only if
                // availability is false, will the app go though the location setting request builder.
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest);
                PendingResult<LocationSettingsResult> result =
                        LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

                result.setResultCallback(LocationService.this);
                resolveLocationSettings = false;
            }
        }
    }

    /* package */ static class LocationStatusCallback implements ResultCallback<Status> {

        @Override
        public void onResult(Status status) {
            if (status.isSuccess()) {
                Log.d(TAG, "Location request successful");
            }
            else {
                Log.d(TAG, "Location request unsuccessful");
            }
        }
    }

}
