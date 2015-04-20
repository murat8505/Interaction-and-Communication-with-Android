package com.example.bchan92.homework1.adapter;

import android.content.Context;
import android.widget.SimpleCursorAdapter;

import com.example.bchan92.homework1.R;
import com.example.bchan92.homework1.provider.CurrentConditionsContract;

/**
 * Created by Brian on 4/18/15.
 */
public class CurrentConditionsAdapter extends SimpleCursorAdapter {

    public static final String[] PROJECTION = new String[] {
            CurrentConditionsContract.Columns._ID,
            CurrentConditionsContract.Columns.CURRENT_CONDITION,
            CurrentConditionsContract.Columns.CURRENT_TEMP,
            CurrentConditionsContract.Columns.HUMIDITY,
            CurrentConditionsContract.Columns.WIND_SPEED
    };

    public static final String[] COLUMN_MAP = new String[] {
            CurrentConditionsContract.Columns.CURRENT_CONDITION,
            CurrentConditionsContract.Columns.CURRENT_TEMP,
            CurrentConditionsContract.Columns.HUMIDITY,
            CurrentConditionsContract.Columns.WIND_SPEED
    };

    public static final int[] ID_MAP = new int[] {
            R.id.current_condition_status,
            R.id.current_temp_text,
            R.id.humidity_text,
            R.id.wind_speed_text
    };

    public CurrentConditionsAdapter(Context context) {
        super(context, R.layout.activity_forecast, null, COLUMN_MAP, ID_MAP, 0);
    }
}
