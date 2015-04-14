package com.example.bchan92.homework1.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.bchan92.homework1.BuildConfig;

public class DayContract {
    public interface Columns extends BaseColumns {
        public static final String TEMPERATURE = "temperature";
        public static final String CONDITION_STRING = "condition_string";
        public static final String TIME = "time";
    }

    public static final long NO_DAY_ID = -1;

    /* package */ static final String TABLE = "Day";

    /* package */ static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.DayProvider";

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
                    Columns.TEMPERATURE + " REAL " +
                    Columns.CONDITION_STRING + " REAL " +
                    Columns.TIME + " TEXT " + // CHECK
                    " )";
}
