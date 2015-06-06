package com.example.joeroger.samplemaps.fragment;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;
import android.text.format.DateFormat;

import com.example.joeroger.samplemaps.utils.IncidentUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;


public class IncidentMarkerMapFragment extends BaseIncidentMapFragment
implements GoogleMap.OnInfoWindowClickListener {

    // This is a more memory efficient data structure than a standard java map. Memory is your
    // enemy in mobile
    private SimpleArrayMap<Marker, Long> markerMap = new SimpleArrayMap<>();


    public static IncidentMarkerMapFragment newInstance() {
        return new IncidentMarkerMapFragment();
    }

    public IncidentMarkerMapFragment() {
        super();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        googleMap.setOnInfoWindowClickListener(this);
    }

    @Override
    protected void addIncidents(@NonNull Cursor cursor) {

        cursor.moveToPosition(-1);
        markerMap.ensureCapacity(cursor.getCount());
        java.text.DateFormat dateFormat = DateFormat.getDateFormat(getActivity());
        java.text.DateFormat timeFormat = DateFormat.getTimeFormat(getActivity());
        Date eventTime = new Date();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(ID_POS);

            // Avoid creating duplicate markers if processing an updated cursor, as this is expensive.
            if (!markerMap.containsValue(id)) {
                eventTime.setTime(cursor.getLong(CLEARANCE_DATE));
                String date = dateFormat.format(eventTime);
                String time = timeFormat.format(eventTime);

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(cursor.getDouble(LATITUDE_POS), cursor.getDouble(LONGITUDE_POS)))
                        .title(cursor.getString(DESCRIPTION_POS))
                        .snippet(date + " " + time)
                        .icon(BitmapDescriptorFactory.defaultMarker(IncidentUtils.translateCategoryToColor(cursor.getString(CATEGORY_POS))));
                markerMap.put(map.addMarker(markerOptions), id);
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Long idLong = markerMap.get(marker);
        if (idLong != null) {
            listener.onIncidentSelected(idLong, marker.getPosition());
        }
    }
}
