package com.example.bchan92.homework1.adapter;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

import com.example.bchan92.homework1.R;
import com.example.bchan92.homework1.provider.ForecastContract;

public class ForecastListAdapter extends SimpleCursorAdapter {

//    public static final String[] PROJECTION = new String[] {
//            ForecastContract.Columns._ID,
//            ForecastContract.Columns.NAME
//    };
//
//    public static final String[] COLUMN_MAP = new String[] {
//            ContactContract.Columns.NAME
//    };
//
//    public static final int[] ID_MAP = new int[] {
//            R.id.text1
//    };

    public ForecastListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
    }
}
