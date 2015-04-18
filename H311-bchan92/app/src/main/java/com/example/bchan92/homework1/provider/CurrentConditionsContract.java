package com.example.bchan92.homework1.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.bchan92.homework1.BuildConfig;

public class CurrentConditionsContract {

    public interface Columns extends BaseColumns {
        public static final String CURRENT_CONDITION = "current_condition";
        public static final String CURRENT_TEMP = "current_temp";
        public static final String HUMIDITY = "humidity";
        public static final String WIND_SPEED = "wind_speed";
        public static final String CITY_NAME = "city_name";
    }

    public interface Json {
        public static final String CURRENT_CONDITION = "description";
        public static final String CURRENT_TEMP = "temp";
        public static final String HUMIDITY = "humidity";
        public static final String WIND_SPEED = "speed";
        public static final String CITY_NAME = "name";
    }

    public static final long NO_CONDITIONS_ID = -1;

    /* package */ static final String TABLE = "CurrentConditions";

    /* package */ static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.CurrentConditionsProvider";

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
                    Columns.CURRENT_CONDITION + " TEXT " +
                    Columns.CURRENT_TEMP + " REAL " +
                    Columns.HUMIDITY + " REAL " +
                    Columns.WIND_SPEED + " REAL " +
                    Columns.CITY_NAME + " TEXT " +
                    " )";
}
