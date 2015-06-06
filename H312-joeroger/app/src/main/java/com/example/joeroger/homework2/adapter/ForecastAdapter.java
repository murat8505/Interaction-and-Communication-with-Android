package com.example.joeroger.homework2.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.joeroger.homework2.R;
import com.example.joeroger.homework2.network.DataFetcher;
import com.example.joeroger.homework2.provider.ForecastContract;
import com.squareup.picasso.Picasso;

import java.util.Date;

/**
 * Created by jrogers on 4/19/15.
 */
public class ForecastAdapter extends CursorAdapter {

    public static final String[] PROJECTION = new String[]{
            ForecastContract.Columns._ID,
            ForecastContract.Columns.DATA_TIME,
            ForecastContract.Columns.ICON,
            ForecastContract.Columns.CONDITION,
            ForecastContract.Columns.TEMPERATURE,
            ForecastContract.Columns.WIND_DIRECTION,
            ForecastContract.Columns.WIND_SPEED
    };

    private static final int ID_POS = 0;
    private static final int DATA_TIME_POS = 1;
    private static final int ICON_POS = 2;
    private static final int CONDITION_POS = 3;
    private static final int TEMPERATURE_POS = 4;
    private static final int WIND_DIRECTION_POS = 5;
    private static final int WIND_SPEED_POS = 6;

    public ForecastAdapter(Context context) {
        super(context, null, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_forecast_item, parent, false);
        view.setClickable(false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.time.setText(formatTime(context, cursor.getLong(DATA_TIME_POS)));
        holder.condition.setText(cursor.getString(CONDITION_POS));
        holder.wind.setText(context.getString(R.string.wind_two_line, cursor.getString(WIND_DIRECTION_POS), cursor.getDouble(WIND_SPEED_POS)));
        holder.temperature.setText(context.getString(R.string.temperature, Math.round(cursor.getDouble(TEMPERATURE_POS))));
        Picasso.with(holder.icon.getContext())
                .load(DataFetcher.buildIconUri(cursor.getString(ICON_POS)))
                .into(holder.icon);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    private static String formatTime(Context context, long time) {
        return DateFormat.getTimeFormat(context).format(new Date(time));
    }

    /* package */ static class ViewHolder {

        final ImageView icon;
        final TextView time;
        final TextView condition;
        final TextView wind;
        final TextView temperature;

        ViewHolder(View view) {
            icon = (ImageView) view.findViewById(R.id.icon);
            time = (TextView) view.findViewById(R.id.time);
            condition = (TextView) view.findViewById(R.id.condition);
            wind = (TextView) view.findViewById(R.id.wind);
            temperature = (TextView) view.findViewById(R.id.temperature);
        }
    }
}
