package com.example.joeroger.homework2.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public final class ForecastContract extends BaseContract {
    public interface Columns extends BaseContract.BaseWeatherColumns {
        public String PERIOD = "period";
        public String TEMPERATURE = "temperature";
    }

    public static final long NO_FORECAST_ID = -1;

    /* package */ static final String TABLE = "forecast";

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
                    BaseContract.BaseWeatherColumns.CITY_ID + " INTEGER NOT NULL, " +
                    Columns.PERIOD + " INTEGER NOT NULL, " +
                    BaseContract.BaseWeatherColumns.CITY_NAME + " TEXT NOT NULL, " +
                    BaseContract.BaseWeatherColumns.ICON + " TEXT, " +
                    BaseContract.BaseWeatherColumns.CONDITION + " TEXT, " +
                    BaseContract.BaseWeatherColumns.HUMIDITY + " INTEGER, " +
                    BaseContract.BaseWeatherColumns.PRESSURE + " REAL, " +
                    BaseContract.BaseWeatherColumns.WIND_SPEED + " REAL, " +
                    BaseContract.BaseWeatherColumns.WIND_DIRECTION + " REAL, " +
                    BaseContract.BaseWeatherColumns.DATA_TIME + " INTEGER, " +
                    BaseContract.BaseWeatherColumns.UPDATED + " INTEGER NOT NULL, " +
                    Columns.TEMPERATURE + " REAL " +
                    ")";

    // Adding an index for the FK. This dramatically helps performance and prevent global table locking.
    /* package */ static final String FORECAST_UNIQUE_INDEX = "FORECAST_UNQ_IDX";

    /* package */ static final String CREATE_FORECAST_UNIQUE_INDEX =
            "CREATE UNIQUE INDEX IF NOT EXISTS " + FORECAST_UNIQUE_INDEX + " ON " + TABLE +
                    " (" + Columns.CITY_ID + ", " + Columns.PERIOD + ")";

    // Adding an index for the unique index.
    /* package */ static final String FORECAST_UNIQUE_DT_INDEX = "FORECAST_UNQ_DT_IDX";

    /* package */ static final String CREATE_FORECAST_UNIQUE_DT_INDEX =
            "CREATE UNIQUE INDEX IF NOT EXISTS " + FORECAST_UNIQUE_DT_INDEX + " ON " + TABLE +
                    " (" + Columns.CITY_ID + ", " + Columns.DATA_TIME + ")";

}
