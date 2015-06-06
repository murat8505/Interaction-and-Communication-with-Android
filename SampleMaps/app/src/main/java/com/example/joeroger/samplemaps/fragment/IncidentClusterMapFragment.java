package com.example.joeroger.samplemaps.fragment;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.util.SimpleArrayMap;
import android.text.format.DateFormat;

import com.example.joeroger.samplemaps.utils.IncidentUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;
import java.util.Date;


public class IncidentClusterMapFragment extends BaseIncidentMapFragment
implements ClusterManager.OnClusterItemInfoWindowClickListener<IncidentClusterMapFragment.MapClusterItem> {

    // This is a more memory efficient data structure than a standard java map. Memory is your
    // enemy in mobile
    private SimpleArrayMap<Long, MapClusterItem> markerMap = new SimpleArrayMap<>();

    private ClusterManager<MapClusterItem> clusterManager;

    public static IncidentClusterMapFragment newInstance() {
        return new IncidentClusterMapFragment();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        clusterManager = new ClusterManager<>(getActivity().getApplicationContext(), googleMap);
        MapClusterRenderer mapClusterRenderer = new MapClusterRenderer(getActivity().getApplicationContext(), googleMap, clusterManager);
        clusterManager.setRenderer(mapClusterRenderer);
        clusterManager.setOnClusterItemInfoWindowClickListener(this);
        googleMap.setOnCameraChangeListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);
        googleMap.setOnInfoWindowClickListener(clusterManager);

        super.onMapReady(googleMap);
    }

    @Override
    protected void addIncidents(@NonNull Cursor cursor) {

        cursor.moveToPosition(-1);
        markerMap.ensureCapacity(cursor.getCount());

        java.text.DateFormat dateFormat = DateFormat.getDateFormat(getActivity());
        java.text.DateFormat timeFormat = DateFormat.getTimeFormat(getActivity());
        Date eventTime = new Date();

        ArrayList<MapClusterItem> items = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            long id = cursor.getLong(ID_POS);

            // Avoid creating duplicate markers if processing an updated cursor, as this is expensive.
            if (markerMap.containsKey(id)) {
                items.add(markerMap.get(id));
            }
            else {
                LatLng position = new LatLng(cursor.getDouble(LATITUDE_POS), cursor.getDouble(LONGITUDE_POS));
                String description = cursor.getString(DESCRIPTION_POS);
                String category = cursor.getString(CATEGORY_POS);
                eventTime.setTime(cursor.getLong(CLEARANCE_DATE));
                String date = dateFormat.format(eventTime);
                String time = timeFormat.format(eventTime);

                MapClusterItem item = new MapClusterItem(cursor.getLong(ID_POS), position, description, category, date + " " + time);
                markerMap.put(id, item);
                items.add(item);
            }
        }

        clusterManager.clearItems();
        clusterManager.addItems(items);

        // This is needed to force the cluster manger to compute the cluster data.
        clusterManager.cluster();
    }

    @Override
    public void onClusterItemInfoWindowClick(MapClusterItem mapClusterItem) {
        listener.onIncidentSelected(mapClusterItem.id, mapClusterItem.position);
    }

    /* package */ static class MapClusterItem implements ClusterItem {

        final long id;
        final LatLng position;
        final String description;
        final String category;
        final String date;

        MapClusterItem(long id, LatLng position, String description, String category, String date) {
            this.id = id;
            this.position = position;
            this.description = description;
            this.category = category;
            this.date = date;
        }

        @Override
        public LatLng getPosition() {
            return position;
        }
    }

    /* package */ static class MapClusterRenderer extends DefaultClusterRenderer<MapClusterItem> {
        public MapClusterRenderer(Context context, GoogleMap map, ClusterManager<MapClusterItem> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(MapClusterItem item, MarkerOptions markerOptions) {
            super.onBeforeClusterItemRendered(item, markerOptions);
            markerOptions.title(item.description);
            markerOptions.snippet(item.date);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(IncidentUtils.translateCategoryToColor(item.category)));
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MapClusterItem> cluster, MarkerOptions markerOptions) {
            super.onBeforeClusterRendered(cluster, markerOptions);
            int majorCrimes = 0;
            for (MapClusterItem item : cluster.getItems()) {
                if (IncidentUtils.isMajor(item.category)) {
                    ++majorCrimes;
                }
            }
            markerOptions.title("Major crimes: " + majorCrimes);
        }
    }
}
