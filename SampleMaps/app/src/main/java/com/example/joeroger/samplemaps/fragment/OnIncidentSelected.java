package com.example.joeroger.samplemaps.fragment;

import com.google.android.gms.maps.model.LatLng;


public interface OnIncidentSelected {
    void onIncidentSelected(long incidentId, LatLng position);
}
