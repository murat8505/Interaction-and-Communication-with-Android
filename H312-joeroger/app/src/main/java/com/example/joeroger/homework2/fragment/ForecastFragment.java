package com.example.joeroger.homework2.fragment;


import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.joeroger.homework2.R;
import com.example.joeroger.homework2.adapter.ForecastAdapter;
import com.example.joeroger.homework2.loader.DailyForecastLoaderCallbacks;
import com.example.joeroger.homework2.loader.ForecastLoaderCallbacks;
import com.example.joeroger.homework2.network.DataFetcher;
import com.example.joeroger.homework2.provider.DailyForecastContract;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment implements
        DailyForecastLoaderCallbacks.OnDailyForecastLoaded,
        ForecastLoaderCallbacks.OnForecastLoaded {

    private static final String STATE_IDLE_TIME = "idleTime";
    private static final long ONE_HOUR = 60 * 60 * 1000;

    private static final String[] DAILY_FORECAST_PROJECTION = new String[]{
            DailyForecastContract.Columns.ICON,
            DailyForecastContract.Columns.CONDITION,
            DailyForecastContract.Columns.MAX_TEMP,
            DailyForecastContract.Columns.WIND_DIRECTION,
            DailyForecastContract.Columns.WIND_SPEED,
            DailyForecastContract.Columns.HUMIDITY,
            DailyForecastContract.Columns.PRESSURE
    };

    private static final int ICON_POS = 0;
    private static final int CONDITION_POS = 1;
    private static final int TEMPERATURE_POS = 2;
    private static final int WIND_DIRECTION_POS = 3;
    private static final int WIND_SPEED_POS = 4;
    private static final int HUMIDITY_POS = 5;
    private static final int PRESSURE_POS = 6;

    private boolean isCreated = false;
    private long cityId;
    private long day;
    private long idleTime = System.currentTimeMillis();
    private ForecastAdapter adapter;

    public ForecastFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forecast, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        adapter = new ForecastAdapter(view.getContext());
        holder.list.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DailyForecastLoaderCallbacks.initLoader(getLoaderManager(), getActivity(), this, DAILY_FORECAST_PROJECTION, cityId, day);
        ForecastLoaderCallbacks.initLoader(getLoaderManager(), getActivity(), this, ForecastAdapter.PROJECTION, cityId, day);
        isCreated = true;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        long now = System.currentTimeMillis();
        idleTime = savedInstanceState != null ? savedInstanceState.getLong(STATE_IDLE_TIME, now) : now;
    }

    @Override
    public void onResume() {
        super.onResume();

        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        Calendar dayTime = Calendar.getInstance(TimeZone.getDefault());
        dayTime.setTimeInMillis(day);

        // If user resumes after an hour, or we have switched to tomorrow, finish this activity
        // and resume to current conditions. This is not a requirement, but something I'm playing
        // with with respect to using the app. Felt odd that I would restart in the forecast and
        // even worse was to restart for a day that no longer could occur.
        if (now.getTimeInMillis() - idleTime > ONE_HOUR ||
                (now.after(dayTime) && now.get(Calendar.DATE) != dayTime.get(Calendar.DATE))) {
            getActivity().finish();
        }

        ForecastLoaderCallbacks.restartLoader(getLoaderManager(), getActivity(), this, ForecastAdapter.PROJECTION, cityId, day);
    }

    @Override
    public void onPause() {
        idleTime = System.currentTimeMillis();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_IDLE_TIME, idleTime);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.swapCursor(null);
        adapter = null;
    }

    @Override
    public void onDailyForecastLoaded(Cursor cursor) {
        if (cursor == null) return;

        ViewHolder holder = getViewHolder();
        if (holder != null && cursor.moveToFirst()) {
            holder.description.setText(formatDay());
            holder.temp.setText(getString(R.string.temperature, Math.round(cursor.getDouble(TEMPERATURE_POS))));
            holder.condition.setText(cursor.getString(CONDITION_POS));
            holder.humidity.setText(getString(R.string.humidity, cursor.getInt(HUMIDITY_POS)));
            holder.pressure.setText(getString(R.string.pressure, cursor.getDouble(PRESSURE_POS)));
            holder.wind.setText(getString(R.string.wind,
                    cursor.getString(WIND_DIRECTION_POS),
                    cursor.getDouble(WIND_SPEED_POS)));
            Picasso.with(holder.icon.getContext())
                    .load(DataFetcher.buildIconUri(cursor.getString(ICON_POS)))
                    .into(holder.icon);
        }
    }

    @Override
    public void onForecastLoaded(Cursor cursor) {
        if (adapter != null) {
            adapter.swapCursor(cursor);
        }
    }

    public void setCityIdAndDay(long cityId, long day) {
        this.cityId = cityId;
        this.day = day;
        if (isCreated) {
            DailyForecastLoaderCallbacks.restartLoader(getLoaderManager(), getActivity(), this, DAILY_FORECAST_PROJECTION, cityId, day);
            ForecastLoaderCallbacks.restartLoader(getLoaderManager(), getActivity(), this, ForecastAdapter.PROJECTION, cityId, day);
        }
    }

    private String formatDay() {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        return formatter.format(new Date(day));
    }

    @Nullable
    protected ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    protected static class ViewHolder {
        final ListView list;
        final TextView description;
        final ImageView icon;
        final TextView temp;
        final TextView condition;
        final TextView wind;
        final TextView pressure;
        final TextView humidity;

        ViewHolder(View view) {
            list = (ListView) view.findViewById(R.id.forecast_list);
            View header = LayoutInflater.from(view.getContext()).inflate(R.layout.fragment_conditions, list, false);
            list.addHeaderView(header, null, false);
            list.setEmptyView(view.findViewById(R.id.forecast_empty));
            description = (TextView) header.findViewById(R.id.description);
            icon = (ImageView) header.findViewById(R.id.icon);
            temp = (TextView) header.findViewById(R.id.temperature);
            header.findViewById(R.id.temp_desc).setVisibility(View.VISIBLE);
            condition = (TextView) header.findViewById(R.id.condition);
            wind = (TextView) header.findViewById(R.id.wind);
            pressure = (TextView) header.findViewById(R.id.pressure);
            humidity = (TextView) header.findViewById(R.id.humidity);
        }
    }
}
