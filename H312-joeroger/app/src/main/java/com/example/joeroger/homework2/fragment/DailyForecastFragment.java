package com.example.joeroger.homework2.fragment;


import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.joeroger.homework2.R;
import com.example.joeroger.homework2.adapter.DailyForecastAdapter;
import com.example.joeroger.homework2.loader.DailyForecastLoaderCallbacks;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 */
public class DailyForecastFragment extends Fragment
implements DailyForecastLoaderCallbacks.OnDailyForecastLoaded,
        AdapterView.OnItemClickListener {

    private DailyForecastFragmentListener listener;
    private long cityId;
    private DailyForecastAdapter adapter;
    private boolean isCreated = false;
    private Calendar nextLoadStart = Calendar.getInstance(TimeZone.getDefault());

    public interface DailyForecastFragmentListener {
        public void onDaySelected(long cityId, long day);
    }

    public DailyForecastFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;
        try {
            listener = (DailyForecastFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName()
                    + " must implement DailyForecastFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_daily_forecast, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridView gridView = (GridView) view.findViewById(R.id.daily_grid);
        adapter = new DailyForecastAdapter(gridView.getContext());
        gridView.setOnItemClickListener(this);
        gridView.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DailyForecastLoaderCallbacks.initLoader(getLoaderManager(), getActivity(), this, DailyForecastAdapter.PROJECTION, cityId);
        nextLoadStart = Calendar.getInstance(TimeZone.getDefault());
        nextLoadStart.add(Calendar.HOUR_OF_DAY, 1);
        isCreated = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        // If user resumes, but was gone for an hour, restart the loader.
        if (nextLoadStart.getTimeInMillis() < System.currentTimeMillis()) {
            DailyForecastLoaderCallbacks.restartLoader(getLoaderManager(), getActivity(), this, DailyForecastAdapter.PROJECTION, cityId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.swapCursor(null);
        adapter = null;
        isCreated = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDailyForecastLoaded(Cursor cursor) {
        if (adapter != null) {
            adapter.swapCursor(cursor);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        long day = adapter.getDateTime(position);
        listener.onDaySelected(cityId, day);
    }

    public void setCityId(long cityId) {
        this.cityId = cityId;
        if (isCreated) {
            DailyForecastLoaderCallbacks.restartLoader(getLoaderManager(), getActivity(), this, DailyForecastAdapter.PROJECTION, cityId);
        }
    }
}
