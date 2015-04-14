package com.example.bchan92.homework1.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.bchan92.homework1.BuildConfig;

public final class ForecastContract {

    public interface Columns extends BaseColumns {
        public static final String HIGH_TEMP = "high_temp";
        public static final String LOW_TEMP = "low_temp";
        public static final String DAY_OF_THE_WEEK = "day_of_the_week";
    }

    public static final long NO_FORECAST_ID = -1;

    /* package */ static final String TABLE = "Forecast";

    /* package */ static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.ForecastProvider";

    /* package */ static final Uri BASE_URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .build();

    public static final Uri URI = BASE_URI;

    // These are the "types" of data that may be returned for various URIs. The first is when multiple
    // items are returned. For example fetch all states.  The second is used when a specific item is returned.
    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + TABLE;

    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + TABLE;


    /* package */ static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE + " ( " +
                    BaseColumns._ID + " INTEGER PRIMARY KEY, " +
                    Columns.HIGH_TEMP + " REAL " +
                    Columns.LOW_TEMP + " REAL " +
                    Columns.DAY_OF_THE_WEEK + " TEXT " +
                    " )";
}
