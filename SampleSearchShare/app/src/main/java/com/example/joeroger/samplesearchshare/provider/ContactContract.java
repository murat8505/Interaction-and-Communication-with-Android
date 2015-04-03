package com.example.joeroger.samplesearchshare.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Basic contact contract
 */
public final class ContactContract {

    public interface Columns extends BaseColumns {
        public static final String NAME = "name";
        public static final String ADDRESS = "address";
        public static final String BIRTH_DATE = "birth_date";
    }

    public static final long NO_CONTACT_ID = -1;

    /* package */ static final String TABLE = "contact";

    /* package */ static final String AUTHORITY = ContactProvider.class.getName();

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
                    Columns.NAME + " TEXT NOT NULL, " +
                    Columns.ADDRESS + " TEXT NOT NULL, " +
                    Columns.BIRTH_DATE + " INTEGER NOT NULL" +
                    " )";

}
