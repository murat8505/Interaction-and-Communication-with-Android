package com.example.joeroger.samplemaps.fragment;


import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.example.joeroger.samplemaps.utils.LocationUtils;

import java.util.List;

/**
 * UI less fragment to determine location
 */
public class AndroidLocationFragment extends Fragment
        implements LocationListener {

    private static final String TAG = "AndroidLocationFragment";

    private static final long ONE_MINUTE = 60 * 1000;
    private static final float MIN_DISTANCE = 250; // meters

    private LocationFragmentListener listener;
    private Location lastLocation;
    LocationManager locationManager;


    public static AndroidLocationFragment newInstance() {
        return new AndroidLocationFragment();
    }

    public AndroidLocationFragment() {
        // Required empty public constructor
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
    }

    @Override
    public void onStart() {
        super.onStart();

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        startLocation();
    }

    @Override
    public void onStop() {
        // ensure location requests are shutdown.
        locationManager.removeUpdates(this);
        locationManager = null;
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void startLocation() {

        Location lastKnownLocation = requestLastKnownLocation();
        lastLocation = LocationUtils.isBetterLocation(lastKnownLocation, lastLocation) ? lastKnownLocation : lastLocation;

        // The location may be really old, so only use it to populate the UI while user waits for
        // a fresh location, if it is acceptable.
        if (LocationUtils.isLocationAcceptable(lastLocation)) {
            updateLocation(lastLocation);
        }

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        // Check to see if location services are enabled. If not, you need to manually put
        // up your own dialog and send user to settings.  (Not shown in this example).
        if (isLocationServiceEnabled()) {

            // Request updates every 60 sec minimum, or if user moves 250 meter minimum.
            // Null indicates not providing a secondary thread to send updates too. If you have
            // complex logic that is performed on update, you should provide a looper, or use
            // pending requests.
            locationManager.requestLocationUpdates(ONE_MINUTE, MIN_DISTANCE, criteria, this, null);

            // If you only want a single location update, this method is better...
            // locationManager.requestSingleUpdate(criteria, this, null);
        }
    }

    private boolean isLocationServiceEnabled() {
        List<String> providers = locationManager.getProviders(true);
        return providers.size() > 0;
    }


    private Location requestLastKnownLocation() {
        // Get all providers, both enabled and disabled
        List<String> providers = locationManager.getProviders(false);
        Location bestLocation = null;

        // Loop over all of the providers, GPS, NETWORK, etc looking for the best location.
        for (String provider : providers) {
            Location location = locationManager.getLastKnownLocation(provider);
            bestLocation = LocationUtils.isBetterLocation(location, bestLocation) ? location : bestLocation;
        }

        Log.d(TAG, "requestLastKnownLocation: " + bestLocation);

        return bestLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: " + location);

        // At this point, trust that the OS is sending us the best location at the moment, so
        // use it even if no other location
        if (LocationUtils.isBetterLocation(location, lastLocation)) {
            updateLocation(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // A provider status changes. This could be say the network provider becoming
        // available because the user moved into an area with better network connectivity.
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Useful if you are waiting for a specific provider is made available by the user
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Notifies a provider is no longer available because the user disabled it via settings
    }

    private void updateLocation(Location location) {
        listener.onNewLocation(location);
        lastLocation = location;
        LocationUtils.saveLastLocation(getActivity(), location);
    }
}
