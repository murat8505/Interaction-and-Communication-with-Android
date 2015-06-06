package com.example.joeroger.samplemaps.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.example.joeroger.samplemaps.BuildConfig;
import com.example.joeroger.samplemaps.loader.IncidentLoaderCallbacks;
import com.example.joeroger.samplemaps.provider.Seattle911Contract;
import com.example.joeroger.samplemaps.utils.LocationUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Base fragment for incidents providing common functionality
 */
public abstract class BaseIncidentMapFragment extends SupportMapFragment
        implements OnMapReadyCallback,
        LocationSource,
        IncidentLoaderCallbacks.OnIncidentLoaderListener {

    private static final String STATE_CAMERA_POSITION = "cameraPosition";

    private static final String WHERE_CLEARANCE_DATE = Seattle911Contract.Columns.CLEARANCE_DATE + " > ?";

    private static final String[] PROJECTION = {
            Seattle911Contract.Columns._ID,
            Seattle911Contract.Columns.LATITUDE,
            Seattle911Contract.Columns.LONGITUDE,
            Seattle911Contract.Columns.DESCRIPTION,
            Seattle911Contract.Columns.LOCATION,
            Seattle911Contract.Columns.CATEGORY,
            Seattle911Contract.Columns.CLEARANCE_DATE
    };

    protected static final int ID_POS = 0;
    protected static final int LATITUDE_POS = 1;
    protected static final int LONGITUDE_POS = 2;
    protected static final int DESCRIPTION_POS = 3;
    protected static final int LOCATION_POS = 4;
    protected static final int CATEGORY_POS = 5;
    protected static final int CLEARANCE_DATE = 6;

    private static final long ONE_DAY = 24 * 60 * 60 * 1000;
    private static final long TWELVE_HOURS = 12 * 60 * 60 * 1000;

    protected GoogleMap map;
    private LocationSource.OnLocationChangedListener locationListener;
    protected CameraPosition cameraPosition;
    protected OnIncidentSelected listener;


    protected BaseIncidentMapFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;
        try {
            listener = (OnIncidentSelected) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnIncidentSelected");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            cameraPosition = savedInstanceState.getParcelable(STATE_CAMERA_POSITION);
        }
        getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cameraPosition != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraPosition.target, cameraPosition.zoom));
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (map != null) {
            cameraPosition = map.getCameraPosition();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_CAMERA_POSITION, cameraPosition);
    }

    @Override
    public void onDestroyView() {
        map = null;
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        UiSettings settings = map.getUiSettings();
        // The toolbar allows user to access navigation/maps for any data point. Handy, but not
        // supporting user to visit crime scenes easily.
        settings.setMapToolbarEnabled(false);

        // If using an emulator, you need to enable zoom controls to be able to zoom in/out as
        // unable to pinch on emulator. This is a potential way to tell if emulator.
        if (BuildConfig.DEBUG && Build.BRAND.startsWith("generic")) {
            settings.setZoomControlsEnabled(true);
        }

        map.setMyLocationEnabled(true);
        map.setLocationSource(this);

        if (cameraPosition == null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(LocationUtils.SEATTLE_LATLNG, 14);
            map.moveCamera(cameraUpdate);
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        locationListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        locationListener = null;
    }

    public void setLocation(Location location) {
        if (locationListener != null) {
            locationListener.onLocationChanged(location);
        }

        if (cameraPosition == null) {
            Location north = LocationUtils.computeLocation(location, 0.5f, 0);
            Location east = LocationUtils.computeLocation(location, 0.5f, 90);
            Location south = LocationUtils.computeLocation(location, 0.5f, 180);
            Location west = LocationUtils.computeLocation(location, 0.5f, 270);

            LatLng ne = new LatLng(north.getLatitude(), east.getLongitude());
            LatLng sw = new LatLng(south.getLatitude(), west.getLongitude());
            LatLngBounds bounds = new LatLngBounds(sw, ne);


            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 4));
        }
        /*double latitude1 = north.getLatitude() < south.getLatitude() ? north.getLatitude() : south.getLatitude();
        double latitude2 = latitude1 == north.getLatitude() ? south.getLatitude() : north.getLatitude();
        double longitude1 = east.getLongitude() < west.getLongitude() ? east.getLongitude() : west.getLongitude();
        double longitude2 = longitude1 == east.getLongitude() ? west.getLongitude() : east.getLongitude();

        String[] selectionArgs = new String[]{
                Double.toString(latitude1),
                Double.toString(latitude2),
                Double.toString(longitude1),
                Double.toString(longitude2)};

        IncidentLoaderCallbacks.restartLoader(getActivity(), getLoaderManager(), this, PROJECTION, WHERE_LOCATION_BETWEEN, selectionArgs);*/
        String[] selectionArgs = new String[]{Long.toString(System.currentTimeMillis() - ONE_DAY)};
        IncidentLoaderCallbacks.restartLoader(getActivity(), getLoaderManager(), this, PROJECTION, WHERE_CLEARANCE_DATE, selectionArgs);
    }

    @Override
    public void onIncidentLoadComplete(@Nullable Cursor cursor) {
        if (cursor == null || map == null) return;

        addIncidents(cursor);
    }

    protected abstract void addIncidents(@NonNull Cursor cursor);
}
