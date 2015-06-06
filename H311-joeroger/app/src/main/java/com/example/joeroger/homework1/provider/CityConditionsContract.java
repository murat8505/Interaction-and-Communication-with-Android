package com.example.joeroger.homework1.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class CityConditionsContract extends BaseContract {

    // pressure conversion 33.8638866667
    public interface Columns extends BaseWeatherColumns {
        public String TEMPERATURE = "temperature";
    }

    public static final long NO_CITY_CONDITIONS_ID = -1;

    /* package */ static final String TABLE = "city_conditions";

    public static final Uri URI = BASE_URI.buildUpon().appendPath(TABLE).build();

    // These are the "types" of data that may be returned for various URIs. The first is when multiple
    // items are returned. For example fetch all states.  The second is used when a specific item is returned.
    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + TABLE;

    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + TABLE;

    /* package */ static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE + " ( " +
                    BaseColumns._ID + " INTEGER PRIMARY KEY, " +
                    BaseWeatherColumns.CITY_ID + " INTEGER NOT NULL UNIQUE, " +
                    BaseWeatherColumns.CITY_NAME + " TEXT NOT NULL, " +
                    BaseWeatherColumns.ICON + " TEXT, " +
                    BaseWeatherColumns.CONDITION + " TEXT, " +
                    BaseWeatherColumns.HUMIDITY + " INTEGER, " +
                    BaseWeatherColumns.PRESSURE + " REAL, " +
                    BaseWeatherColumns.WIND_SPEED + " REAL, " +
                    BaseWeatherColumns.WIND_DIRECTION + " TEXT, " +
                    BaseWeatherColumns.DATA_TIME + " INTEGER, " +
                    BaseWeatherColumns.UPDATED + " INTEGER NOT NULL, " +
                    Columns.TEMPERATURE + " REAL " +
                    ")";
}
