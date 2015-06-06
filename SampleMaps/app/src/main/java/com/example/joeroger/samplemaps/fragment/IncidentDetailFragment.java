package com.example.joeroger.samplemaps.fragment;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.joeroger.samplemaps.R;
import com.example.joeroger.samplemaps.loader.IncidentLoaderCallbacks;
import com.example.joeroger.samplemaps.provider.Seattle911Contract;

import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class IncidentDetailFragment extends Fragment
        implements IncidentLoaderCallbacks.OnIncidentLoaderListener {

    public static final String[] PROJECTION = {
            Seattle911Contract.Columns.DESCRIPTION,
            Seattle911Contract.Columns.LOCATION,
            Seattle911Contract.Columns.CLEARANCE_DATE
    };

    private static final int DESCRIPTION_POS = 0;
    private static final int LOCATION_POS = 1;
    private static final int CLEARANCE_DATE_POS = 2;

    private long incidentId;

    public IncidentDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_incident_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IncidentLoaderCallbacks.initLoader(getActivity(), getLoaderManager(), this, incidentId, PROJECTION);
    }

    @Override
    public void onIncidentLoadComplete(@Nullable Cursor cursor) {
        ViewHolder holder = getViewHolder();
        if (cursor != null && cursor.moveToFirst() && holder != null) {
            holder.description.setText(cursor.getString(DESCRIPTION_POS));
            holder.location.setText(cursor.getString(LOCATION_POS));

            Context context = getActivity();
            Date eventTime = new Date(cursor.getLong(CLEARANCE_DATE_POS));
            String date = DateFormat.getDateFormat(context).format(eventTime);
            String time = DateFormat.getTimeFormat(context).format(eventTime);
            holder.date.setText(date + " " + time);
        }
    }

    public void setIncidentId(long incidentId) {
        this.incidentId = incidentId;
    }

    @Nullable
    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /* package */ static class ViewHolder {
        final TextView description;
        final TextView location;
        final TextView date;

        ViewHolder(View view) {
            description = (TextView) view.findViewById(R.id.description);
            location = (TextView) view.findViewById(R.id.address);
            date = (TextView) view.findViewById(R.id.date);
        }
    }
}
