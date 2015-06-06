package com.example.joeroger.homework1.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.joeroger.R;
import com.example.joeroger.homework1.provider.DailyForecastContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by jrogers on 4/19/15.
 */
public class DailyForecastAdapter extends CursorAdapter {

    public static final String[] PROJECTION = new String[]{
            DailyForecastContract.Columns._ID,
            DailyForecastContract.Columns.DATA_TIME,
            DailyForecastContract.Columns.ICON,
            DailyForecastContract.Columns.MAX_TEMP,
            DailyForecastContract.Columns.MIN_TEMP
    };

    private static final int ID_POS = 0;
    private static final int DATA_TIME_POS = 1;
    private static final int ICON_POS = 2;
    private static final int MAX_TEMP_POS = 3;
    private static final int MIN_TEMP_POS = 4;

    public DailyForecastAdapter(Context context) {
        super(context, null, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_daily_forecast_item, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        SimpleDateFormat format = new SimpleDateFormat("EEE", Locale.getDefault());
        Calendar time = Calendar.getInstance(TimeZone.getDefault());
        time.setTimeInMillis(cursor.getLong(DATA_TIME_POS));
        holder.weekday.setText(format.format(time.getTime()));
        holder.highTemp.setText(context.getString(R.string.temperature, Math.round(cursor.getDouble(MAX_TEMP_POS))));
        holder.lowTemp.setText(context.getString(R.string.temperature, Math.round(cursor.getDouble(MIN_TEMP_POS))));
    }

    public long getDateTime(int position) {
        Cursor cursor = getCursor();
        if (cursor != null && cursor.moveToPosition(position)) {
            return cursor.getLong(DATA_TIME_POS);
        }
        return 0;
    }

    /* package */ static class ViewHolder {
        final TextView weekday;
        final ImageView icon;
        final TextView highTemp;
        final TextView lowTemp;

        ViewHolder(View view) {
            weekday = (TextView) view.findViewById(R.id.weekday);
            icon = (ImageView) view.findViewById(R.id.icon);
            highTemp = (TextView) view.findViewById(R.id.high_temp);
            lowTemp = (TextView) view.findViewById(R.id.low_temp);
        }
    }
}
