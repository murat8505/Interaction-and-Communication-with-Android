package com.example.joeroger.homework2.adapter;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

import com.example.joeroger.homework2.R;
import com.example.joeroger.homework2.provider.CityConditionsContract;


public class CitySpinnerCursorAdapter extends SimpleCursorAdapter {

    public static final String[] PROJECTION = new String[]{
            // Alias city id as the _id in the adapter. Helps to maintain position as data updates
            CityConditionsContract.Columns.CITY_ID + " AS " + CityConditionsContract.Columns._ID,
            CityConditionsContract.Columns.CITY_NAME,
            CityConditionsContract.Columns.FAVORITE
    };

    private static final String[] COLUMNS_FROM = new String[]{
            CityConditionsContract.Columns.CITY_NAME
    };

    private static final int[] RESOURCE_ID_TO = new int[]{
            android.R.id.text1
    };

    public CitySpinnerCursorAdapter(Context context, Cursor c) {
        super(context, R.layout.adapter_material_actionbar_spinner_title_item, c, COLUMNS_FROM, RESOURCE_ID_TO, 0);
        setDropDownViewResource(R.layout.adapter_material_spinner_drop_down_item);
    }
}
