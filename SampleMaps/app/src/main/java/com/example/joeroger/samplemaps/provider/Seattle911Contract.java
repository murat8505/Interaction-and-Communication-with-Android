package com.example.joeroger.samplemaps.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.joeroger.samplemaps.BuildConfig;

/**
 *
 */
public class Seattle911Contract {

    public interface Columns extends BaseColumns {
        public static final String OFFENSE_ID = "offense_id";
        public static final String CATEGORY = "category";
        public static final String SUB_CATEGORY = "subcategory";
        public static final String CLEARANCE_DATE = "clearance_date";
        public static final String DESCRIPTION = "description";
        public static final String LOCATION = "location";
        public static final String LONGITUDE = "longitude";
        public static final String LATITUDE = "latitude";
    }

    public interface Json {
        public static final String OFFENSE_ID = "general_offense_number";
        public static final String CATEGORY = "event_clearance_group";
        public static final String SUB_CATEGORY = "event_clearance_subgroup";
        public static final String CLEARANCE_DATE = "event_clearance_date";
        public static final String DESCRIPTION = "event_clearance_description";
        public static final String LOCATION = "hundred_block_location";
        public static final String LONGITUDE = "longitude";
        public static final String LATITUDE = "latitude";
    }

    public static final long NO_INCIDENT_ID = -1;

    /* package */ static final String TABLE = "seattle911";

    /* package */ static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.Seattle911Provider";

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
                    Columns.OFFENSE_ID + " INTEGER UNIQUE NOT NULL, " +
                    Columns.CATEGORY + " TEXT, " +
                    Columns.SUB_CATEGORY + " TEXT, " +
                    Columns.CLEARANCE_DATE + " INTEGER NOT NULL, " +
                    Columns.DESCRIPTION + " TEXT, " +
                    Columns.LOCATION + " TEXT, " +
                    Columns.LONGITUDE + " REAL, " +
                    Columns.LATITUDE + " REAL" +
                    " )";

}
