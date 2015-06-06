package com.example.joeroger.samplemaps.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.joeroger.samplemaps.BuildConfig;
import com.example.joeroger.samplemaps.utils.LocationUtils;
import com.google.android.gms.common.ConnectionResult;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.lang.ref.WeakReference;

public class LocationService extends Service
        implements LocationServiceMgr,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult> {

    private static final String TAG = "LocationService";

    private static final long TEN_SECONDS = 10 * 1000;
    private static final long ONE_MINUTE = 60 * 1000;
    private static final long ONE_HOUR = 60 * ONE_MINUTE;
    private static final float MIN_DISTANCE = 250; // meters

    private static final Location SEATTLE_LOCATION = new Location("Fused");

    static {
        SEATTLE_LOCATION.setLatitude(LocationUtils.SEATTLE_LATLNG.latitude);
        SEATTLE_LOCATION.setLongitude(LocationUtils.SEATTLE_LATLNG.longitude);
    }

    @NonNull
    public static Intent buildIntent(@NonNull Context context) {
        return new Intent(context, LocationService.class);
    }

    public interface LocationServiceListener {
        void onLocationUpdate(Location location);

        void resolveGPSError(ConnectionResult connectionResult);

        void resolveLocationSettingsError(Status status);
    }


    private IBinder binder = new LocationServiceBinder();
    private WeakReference<LocationServiceListener> listenerRef;
    private GoogleApiClient googleApiClient;
    private LocationCallback locationCallback = new MyLocationCallback();
    private Location lastLocation;
    private LocationRequest locationRequest;
    private LocationRequest backgroundLocationRequest;
    private boolean resolvingError;
    private boolean resolveLocationSettings = true;
    private ConnectionResult connectionResult;
    private Status status;

    public LocationService() {
        Log.d(TAG, "Constructed");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        lastLocation = LocationUtils.getLastLocation(this);

        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // First built the request the fragment will be using to request updates.
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(MIN_DISTANCE);

        // For this app we are only using location to filter 911 events in roughly 1 km rectangle
        // around your current location.

        // How frequent you want to be informed, the slower the better even if you want GPS.
        locationRequest.setInterval(ONE_MINUTE);

        // If another app requests faster updates, what is the max rate you want to be informed
        locationRequest.setFastestInterval(TEN_SECONDS);

        // Max time you are willing to wait for update, ideally double your interval. This lets
        // the fused location manager schedule your app with others to reduce battery use, etc
        locationRequest.setMaxWaitTime(2 * ONE_MINUTE);

        // If only want a limited number of updates. Essentially self cancels when done.
        //locationRequest.setNumUpdates(1);

        backgroundLocationRequest = new LocationRequest();
        backgroundLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        backgroundLocationRequest.setSmallestDisplacement(MIN_DISTANCE);
        backgroundLocationRequest.setInterval(TEN_SECONDS);
        backgroundLocationRequest.setMaxWaitTime(2 * ONE_HOUR);
        // Only want one background request.
        backgroundLocationRequest.setNumUpdates(1);
    }

    public class LocationServiceBinder extends Binder implements LocationServiceMgr {

        @Override
        public void onPlayServicesAreAvailable() {
            LocationService.this.onPlayServicesAreAvailable();
        }

        @Override
        public void registerListener(LocationServiceListener listener) {
            LocationService.this.registerListener(listener);
        }

        @Override
        public void unregisterListener(LocationServiceListener listener) {
            LocationService.this.unregisterListener(listener);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        connectionResult = null;
        status = null;
        resolveLocationSettings = true;
        googleApiClient.connect();
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationCallback);
            googleApiClient.disconnect();
        }
        return super.onUnbind(intent);
    }

    public void onPlayServicesAreAvailable() {
        // Notified by the activity that play services are now available. If
        // not connected, or connecting, then restart the connection process.
        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    @Override
    public void registerListener(LocationServiceListener listener) {
        Log.d(TAG, "registerListener");

        LocationServiceListener currentListener = getListener();
        if (currentListener != null && currentListener != listener) {
            throw new IllegalStateException("Listener already registered");
        }

        this.listenerRef = new WeakReference<>(listener);

        if (listener != null) {
            if (connectionResult != null) {
                listener.resolveGPSError(connectionResult);
            }
            else if (status != null) {
                listener.resolveLocationSettingsError(status);
            }
            else if (lastLocation != null) {
                listener.onLocationUpdate(lastLocation);
            }
        }
    }

    @Override
    public void unregisterListener(LocationServiceListener listener) {
        Log.d(TAG, "unregisterListener");

        if (listenerRef != null && listenerRef.get() == listener) {
            listenerRef.clear();
            listenerRef = null;
        }
        else {
            Log.w(TAG, "Attempting to unregister unknown listener");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Connected to Google Play services!
        // The good stuff goes here, if you are able to do things that are outside of UI
        // interaction. For places, it depends on search so this app has nothing to do.
        Log.d(TAG, "Connected");

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
        LocationServiceListener listener = getListener();
        if (listener != null) {
            listener.resolveGPSError(connectionResult);
        }
        else {
            this.connectionResult = connectionResult;
        }
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                requestLocationUpdates();
                return;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. But could be fixed by showing the user
                // a dialog.
                LocationServiceListener listener = getListener();
                if (listener != null) {
                    listener.resolveLocationSettingsError(status);
                    status = null;
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way to fix the
                // settings so we won't show the dialog...
                break;
        }

        // Must be in an error state, fallback to last known location
        requestLastKnownLocation();
    }

    private void requestLocationUpdates() {

        // First refresh using last known location so user doesn't wait for location request.
        requestLastKnownLocation();

        // Request updates using our location request criteria
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationCallback, null);
    }

    private void requestLastKnownLocation() {
        Location newLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (LocationUtils.isBetterLocation(newLocation, lastLocation)) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Using last location: " + newLocation);
            }
            updateLocation(newLocation);
        }
        else if (lastLocation != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Using last saved location: " + lastLocation);
            }
            updateLocation(lastLocation);
        }
    }

    private void updateLocation(Location location) {
        LocationServiceListener listener = getListener();
        if (listener != null) {
            LatLng tester = new LatLng(location.getLatitude(), location.getLongitude());
            listener.onLocationUpdate(LocationUtils.SEATTLE_BOUNDS.contains(tester) ? location : SEATTLE_LOCATION);
        }
        lastLocation = location;
        LocationUtils.saveLastLocation(this, location);
    }

    private LocationServiceListener getListener() {
        return (listenerRef != null) ? listenerRef.get() : null;
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
                updateLocation(location);
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
                        .addLocationRequest(locationRequest)
                        .addLocationRequest(backgroundLocationRequest);
                PendingResult<LocationSettingsResult> result =
                        LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

                result.setResultCallback(LocationService.this);
                resolveLocationSettings = false;
            }
        }
    }
}
