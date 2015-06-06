package com.example.joeroger.samplemaps.fragment;


import android.app.Activity;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.joeroger.samplemaps.R;
import com.example.joeroger.samplemaps.adapter.IncidentAdapter;
import com.example.joeroger.samplemaps.loader.IncidentLoaderCallbacks;
import com.example.joeroger.samplemaps.provider.Seattle911Contract;
import com.example.joeroger.samplemaps.receiver.NetworkStatusBroadcastReceiver;
import com.example.joeroger.samplemaps.utils.LocationUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class IncidentListFragment extends Fragment
        implements IncidentLoaderCallbacks.OnIncidentLoaderListener,
        NetworkStatusBroadcastReceiver.NetworkStatusListener,
        AdapterView.OnItemClickListener {

    private static final long TWELVE_HOURS = 12 * 60 * 60 * 1000;
    private static final String WHERE_CLEARANCE_DATE = Seattle911Contract.Columns.CLEARANCE_DATE + " > ?";

    private static final String WHERE_LOCATION_BETWEEN = "(" +
            Seattle911Contract.Columns.LATITUDE + " between ? and ?) AND (" +
            Seattle911Contract.Columns.LONGITUDE + " between ? and ?)";

    private long animationDuration;
    private NetworkStatusBroadcastReceiver receiver;
    private OnIncidentSelected listener;

    public static IncidentListFragment newInstance() {
        return new IncidentListFragment();
    }

    public IncidentListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        animationDuration = activity.getResources().getInteger(android.R.integer.config_shortAnimTime);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_incident_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        holder.list.setAdapter(new IncidentAdapter(view.getContext()));
        holder.list.setOnItemClickListener(this);
        holder.listContainer.setVisibility(View.GONE);
        holder.progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IncidentLoaderCallbacks.initLoader(getActivity(), getLoaderManager(), this, IncidentAdapter.PROJECTION);
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new NetworkStatusBroadcastReceiver(getActivity(), this);
    }

    @Override
    public void onPause() {
        super.onPause();
        receiver.unregister();
        receiver = null;
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @Override
    public void onIncidentLoadComplete(@Nullable Cursor cursor) {
        ViewHolder holder = getViewHolder();
        if (holder == null) return;

        IncidentAdapter adapter = (IncidentAdapter) holder.list.getAdapter();
        adapter.swapCursor(cursor);
        if (holder.listContainer.getVisibility() != View.VISIBLE) {
            crossFadeViews(holder.listContainer, holder.progress);
        }
    }

    @Override
    public void onError() {
        Toast.makeText(getActivity(), "Error occurred with network", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess() {
        Toast.makeText(getActivity(), "Network data fetch successful", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        listener.onIncidentSelected(id, null);
    }

    public void setLocation(Location location) {
        /*Location north = LocationUtils.computeLocation(location, 0.5f, 0);
        Location east = LocationUtils.computeLocation(location, 0.5f, 90);
        Location south = LocationUtils.computeLocation(location, 0.5f, 180);
        Location west = LocationUtils.computeLocation(location, 0.5f, 270);

        double latitude1 = north.getLatitude() < south.getLatitude() ? north.getLatitude() : south.getLatitude();
        double latitude2 = latitude1 == north.getLatitude() ? south.getLatitude() : north.getLatitude();
        double longitude1 = east.getLongitude() < west.getLongitude() ? east.getLongitude() : west.getLongitude();
        double longitude2 = longitude1 == east.getLongitude() ? west.getLongitude() : east.getLongitude();

        String[] selectionArgs = new String[]{
                Double.toString(latitude1),
                Double.toString(latitude2),
                Double.toString(longitude1),
                Double.toString(longitude2)};

        IncidentLoaderCallbacks.restartLoader(getActivity(), getLoaderManager(), this, IncidentAdapter.PROJECTION, WHERE_LOCATION_BETWEEN, selectionArgs);
        */
        String[] selectionArgs = new String[]{Long.toString(System.currentTimeMillis() - TWELVE_HOURS)};
        IncidentLoaderCallbacks.restartLoader(getActivity(), getLoaderManager(), this, IncidentAdapter.PROJECTION, WHERE_CLEARANCE_DATE, selectionArgs);
    }

    private void crossFadeViews(final View fadeInView, final View fadeOutView) {
        fadeInView.setAlpha(0f);
        fadeInView.setVisibility(View.VISIBLE);

        ViewCompat.animate(fadeInView)
                .alpha(1f)
                .setDuration(animationDuration)
                .setListener(null)
                .withLayer();

        ViewCompat.animate(fadeOutView)
                .alpha(0f)
                .setDuration(animationDuration)
                .setListener(null)
                .withLayer()
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        fadeOutView.setVisibility(View.GONE);
                    }
                });
    }

    @Nullable
    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /* package */ class ViewHolder {
        final View listContainer;
        final ListView list;
        final View progress;

        ViewHolder(View view) {
            listContainer = view.findViewById(R.id.list_container);
            list = (ListView) listContainer.findViewById(R.id.list);
            list.setEmptyView(listContainer.findViewById(R.id.empty));
            progress = view.findViewById(R.id.progress);
        }
    }
}
