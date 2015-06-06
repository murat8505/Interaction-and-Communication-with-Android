package com.example.joeroger.homework2.fragment;


import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.joeroger.homework2.R;
import com.example.joeroger.homework2.loader.CityConditionsLoaderCallbacks;
import com.example.joeroger.homework2.network.DataFetcher;
import com.example.joeroger.homework2.provider.CityConditionsContract;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConditionsFragment extends Fragment
implements CityConditionsLoaderCallbacks.OnCityConditionsLoaded {

    private static final String[] PROJECTION = new String[] {
            CityConditionsContract.Columns.ICON,
            CityConditionsContract.Columns.CONDITION,
            CityConditionsContract.Columns.TEMPERATURE,
            CityConditionsContract.Columns.HUMIDITY,
            CityConditionsContract.Columns.PRESSURE,
            CityConditionsContract.Columns.WIND_DIRECTION,
            CityConditionsContract.Columns.WIND_SPEED
    };

    private static final int ICON_POS = 0;
    private static final int CONDITION_POS = 1;
    private static final int TEMPERATURE_POS = 2;
    private static final int HUMIDITY_POS = 3;
    private static final int PRESSURE_POS = 4;
    private static final int WIND_DIRECTION_POS = 5;
    private static final int WIND_SPEED_POS = 6;

    private boolean isCreated = false;
    private long cityId;

    public ConditionsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CityConditionsLoaderCallbacks.initLoader(getLoaderManager(), getActivity(), this, PROJECTION, cityId);
        isCreated = true;
    }

    @Override
    public void onCityConditionsLoaded(Cursor cursor) {
        if (cursor == null) return;
        ViewHolder holder = getViewHolder();
        if (holder != null && cursor.moveToFirst()) {
            holder.description.setText(R.string.current_conditions);
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

    public void setCityId(long cityId) {
        this.cityId = cityId;
        if (isCreated) {
            CityConditionsLoaderCallbacks.restartLoader(getLoaderManager(), getActivity(), this, PROJECTION, cityId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_conditions, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
    }

    @Nullable
    protected ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    protected static class ViewHolder {
        final TextView description;
        final ImageView icon;
        final TextView temp;
        final TextView condition;
        final TextView wind;
        final TextView pressure;
        final TextView humidity;

        ViewHolder(View view) {
            description = (TextView) view.findViewById(R.id.description);
            icon = (ImageView) view.findViewById(R.id.icon);
            temp = (TextView) view.findViewById(R.id.temperature);
            condition = (TextView) view.findViewById(R.id.condition);
            wind = (TextView) view.findViewById(R.id.wind);
            pressure = (TextView) view.findViewById(R.id.pressure);
            humidity = (TextView) view.findViewById(R.id.humidity);
        }
    }
}
