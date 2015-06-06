package com.example.joeroger.samplelocation.fragment;


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
import android.widget.ListView;

import com.example.joeroger.samplelocation.R;
import com.example.joeroger.samplelocation.adapter.IncidentAdapter;
import com.example.joeroger.samplelocation.loader.IncidentLoaderCallbacks;
import com.example.joeroger.samplelocation.provider.Seattle911Contract;
import com.example.joeroger.samplelocation.utils.LocationUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class IncidentListFragment extends Fragment
        implements IncidentLoaderCallbacks.OnIncidentLoaderListener {

    private static final String WHERE_LOCATION_BETWEEN = "(" +
            Seattle911Contract.Columns.LATITUDE + " between ? and ?) AND (" +
            Seattle911Contract.Columns.LONGITUDE + " between ? and ?)";

    private long animationDuration;

    public IncidentListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        animationDuration = activity.getResources().getInteger(android.R.integer.config_shortAnimTime);
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
        holder.listContainer.setVisibility(View.GONE);
        holder.progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IncidentLoaderCallbacks.initLoader(getActivity(), getLoaderManager(), this, IncidentAdapter.PROJECTION);
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

    public void setLocation(Location location) {
        Location north = LocationUtils.computeLocation(location, 1, 0);
        Location east = LocationUtils.computeLocation(location, 1, 90);
        Location south = LocationUtils.computeLocation(location, 1, 180);
        Location west = LocationUtils.computeLocation(location, 1, 270);

        double latitude1 = north.getLatitude() < south.getLatitude() ? north.getLatitude() : south.getLatitude();
        double latitude2 = latitude1 == north.getLatitude() ? south.getLatitude() : north.getLatitude();
        double longitude1 = east.getLongitude() < west.getLongitude() ? east.getLongitude() : west.getLongitude();
        double longitude2 = longitude1 == east.getLongitude() ? west.getLongitude() : east.getLongitude();

        String[] selectionArgs = new String[]{
                Double.toString(latitude1),
                Double.toString(latitude2),
                Double.toString(longitude1),
                Double.toString(longitude2) };

        IncidentLoaderCallbacks.restartLoader(getActivity(), getLoaderManager(), this, IncidentAdapter.PROJECTION, WHERE_LOCATION_BETWEEN, selectionArgs);
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

    private
    @Nullable
    ViewHolder getViewHolder() {
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
