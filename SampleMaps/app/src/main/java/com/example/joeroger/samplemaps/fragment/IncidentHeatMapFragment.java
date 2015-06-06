package com.example.joeroger.samplemaps.fragment;

import android.database.Cursor;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.example.joeroger.samplemaps.utils.IncidentUtils;
import com.example.joeroger.samplemaps.utils.LocationUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;

/**
 * Created by jrogers on 5/13/15.
 */
public class IncidentHeatMapFragment extends BaseIncidentMapFragment {

    private HeatmapTileProvider heatmapTileProvider;
    private TileOverlay tileOverlay;

    public static IncidentHeatMapFragment newInstance() {
        return new IncidentHeatMapFragment();
    }

    public IncidentHeatMapFragment() {
        super();
    }

    @Override
    public void setLocation(Location location) {
        super.setLocation(location);
        if (cameraPosition == null) {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(LocationUtils.SEATTLE_BOUNDS, 4));
            cameraPosition = map.getCameraPosition();
        }
    }

    @Override
    public void onDestroyView() {
        tileOverlay = null;
        heatmapTileProvider = null;
        super.onDestroyView();
    }

    @Override
    protected void addIncidents(@NonNull Cursor cursor) {

        ArrayList<WeightedLatLng> items = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            LatLng position = new LatLng(cursor.getDouble(LATITUDE_POS), cursor.getDouble(LONGITUDE_POS));
            items.add(new WeightedLatLng(position, IncidentUtils.translateCategoryToIntensity(cursor.getString(CATEGORY_POS))));
        }

        if (heatmapTileProvider == null) {
            heatmapTileProvider = new HeatmapTileProvider.Builder()
                    .weightedData(items)
                    .build();
            tileOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));
        }
        else {
            heatmapTileProvider.setWeightedData(items);
            tileOverlay.clearTileCache();
        }
    }
}
