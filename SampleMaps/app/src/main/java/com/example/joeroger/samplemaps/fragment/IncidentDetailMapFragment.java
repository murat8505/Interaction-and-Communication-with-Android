package com.example.joeroger.samplemaps.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.example.joeroger.samplemaps.loader.IncidentLoaderCallbacks;
import com.example.joeroger.samplemaps.provider.Seattle911Contract;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class IncidentDetailMapFragment extends SupportMapFragment
        implements OnMapReadyCallback,
        IncidentLoaderCallbacks.OnIncidentLoaderListener {

    private static final String[] PROJECTION = new String[] {
            Seattle911Contract.Columns.LATITUDE,
            Seattle911Contract.Columns.LONGITUDE
    };

    private static final int LATITUDE_POS = 0;
    private static final int LONGITUDE_POS = 1;

    private GoogleMap map;
    private long incidentId;
    private LatLng position;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (position == null) {
            IncidentLoaderCallbacks.initLoader(getActivity(), getLoaderManager(), this, incidentId, PROJECTION);
        }
    }

    @Override
    public void onDestroyView() {
        map = null;
        super.onDestroyView();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        UiSettings settings = map.getUiSettings();
        // The toolbar allows user to access navigation/maps for any data point. Handy, but not
        // supporting user to visit crime scenes easily.
        settings.setMapToolbarEnabled(false);
        settings.setAllGesturesEnabled(false);
        settings.setMyLocationButtonEnabled(false);
        settings.setCompassEnabled(false);
        settings.setIndoorLevelPickerEnabled(false);

        map.setMyLocationEnabled(false);

        positionMap();
    }

    @Override
    public void onIncidentLoadComplete(@Nullable Cursor cursor) {

        if (cursor != null && cursor.moveToFirst()) {
            position = new LatLng(cursor.getDouble(LATITUDE_POS), cursor.getDouble(LONGITUDE_POS));
            positionMap();
        }
    }

    private void positionMap() {
        if (map != null && position != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16));
            map.addMarker(new MarkerOptions().position(position));
        }
    }

    public void setIncidentData(long incidentId, LatLng position) {
        this.incidentId = incidentId;
        this.position = position;
    }
}
