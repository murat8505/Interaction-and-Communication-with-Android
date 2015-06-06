package com.example.joeroger.samplelocation.fragment;


import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.example.joeroger.samplelocation.BuildConfig;
import com.example.joeroger.samplelocation.receiver.LocationBroadcastReceiver;
import com.example.joeroger.samplelocation.utils.LocationUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * UI less fragment to determine location
 */
public class GPSLocationFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        PlayServicesDialogFragment.PlayServicesDialogFragmentListener,
        ResultCallback<LocationSettingsResult>,
        LocationListener {

    private static final String TAG = "GPSLocationFragment";

    private static final String GPS_DIALOG_TAG = "GooglePlayServiceDialog";

    private static final String STATE_RESOLVING_ERROR = "resolvingError";

    private static final int RESOLVE_ERROR_REQUEST = 110;
    public static final int CHECK_SETTINGS_REQUEST = 115;

    private static final long TEN_SECONDS = 10 * 1000;
    private static final long ONE_MINUTE = 60 * 1000;
    private static final long ONE_HOUR = 60 * ONE_MINUTE;
    private static final float MIN_DISTANCE = 250; // meters


    private LocationFragmentListener listener;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private LocationRequest backgroundLocationRequest;
    private boolean resolvingError;

    public static GPSLocationFragment newInstance() {
        return new GPSLocationFragment();
    }

    public GPSLocationFragment() {
        // Required empty public constructor
    }


    public void onPlayServicesAreAvailable() {
        resolvingError = false;

        // Notified by the activity that play services are now available. If
        // not connected, or connecting, then restart the connection process.
        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;
        try {
            listener = (LocationFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName()
                    + " must implement LocationFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lastLocation = LocationUtils.getLastLocation(getActivity());

        resolvingError = savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR);

        googleApiClient = new GoogleApiClient
                .Builder(getActivity())
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

    @Override
    public void onStart() {
        super.onStart();

        // If resolving an error, do not start the client... as it is already started.
        if (!resolvingError) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        // Switch to background mode. Not a typical way of requesting background updates, but
        // for a quick example will do. Remember the backgroundLocationRequest only wants one
        // update so it will shutdown eventually.
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient,
                backgroundLocationRequest,
                LocationBroadcastReceiver.buildPendingIntent(getActivity()));
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, resolvingError);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Connected to Google Play services!
        // The good stuff goes here, if you are able to do things that are outside of UI
        // interaction. For places, it depends on search so this app has nothing to do.
        Log.d(TAG, "Connected");

        // Lets see if we have location services enabled...


        // Ensure location service settings are enabled for what we need. If you have different
        // location requests, you may check both at same time
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

        result.setResultCallback(this);
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
        if (resolvingError) {
            // Already attempting to resolve an error.
            return;
        }

        if (connectionResult.hasResolution()) {
            try {
                resolvingError = true;
                connectionResult.startResolutionForResult(getActivity(), RESOLVE_ERROR_REQUEST);
            }
            catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                googleApiClient.connect();
                resolvingError = false;
            }
        }
        else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            // Must use your own fragment wrapper around the dialog or rotation does not work correctly.
            // Luckily you could essentially copy the PlayServicesDialogFragment from project to project.
            // Works with both activities/fragments as parent. However, the onActivityResult() is always
            // routed to the "Activity". So the activity is responsible for notifying any fragments that
            // google play services are available.
            DialogFragment fragment = PlayServicesDialogFragment.newInstance(connectionResult.getErrorCode());
            fragment.show(getChildFragmentManager(), GPS_DIALOG_TAG);
            resolvingError = true;
        }
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
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(getActivity(), CHECK_SETTINGS_REQUEST);
                    return;
                }
                catch (IntentSender.SendIntentException e) {
                    // Ignore the error. Nothing we can do...
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

    @Override
    public void onDialogCancelled() {
        Log.d(TAG, "Dialog cancelled");
        listener.onPlayServicesUnavailable();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onLocationChanged: " + location);
        }
        if (LocationUtils.isBetterLocation(location, lastLocation)) {
            updateLocation(location);
        }
    }

    public void onSettingsEnabled() {
        requestLocationUpdates();
    }

    public void onSettingsNotEnabled() {
        requestLastKnownLocation();
    }

    private void requestLocationUpdates() {

        // First refresh using last known location so user doesn't wait for location request.
        requestLastKnownLocation();

        // Request updates using our location request criteria
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
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
        listener.onNewLocation(location);
        lastLocation = location;
        LocationUtils.saveLastLocation(getActivity(), location);
    }
}
