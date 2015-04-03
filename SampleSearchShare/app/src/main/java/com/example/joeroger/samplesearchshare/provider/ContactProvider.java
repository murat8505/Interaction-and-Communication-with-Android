package com.example.joeroger.samplesearchshare.provider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

public class ContactProvider extends ContentProvider {

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int CONTACT = 1;
    private static final int CONTACT_ID = 2;
    private static final int SEARCH_PRE_QUERY = 3;
    private static final int SEARCH_QUERY = 4;

    static {
        uriMatcher.addURI(ContactContract.AUTHORITY, null, CONTACT);
        uriMatcher.addURI(ContactContract.AUTHORITY, "#", CONTACT_ID);

        // Additional uris to match to handle search. The first, handles the case where the user opens search.
        // You seem to get this even if you specify minimum characters. You could use this as a way to show recent
        // searches to "pre-populate" the dropdown. Users tend to search for the same thing. Or you could do something
        // else that is reasonable like ignore it.
        //
        // For the purposes of this app, we are "rigging" the search to return no data.
        uriMatcher.addURI(ContactContract.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/", SEARCH_PRE_QUERY);

        // The second matches the query. Essentially the last segment will be the query string.
        uriMatcher.addURI(ContactContract.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_QUERY);
    }

    private static final String WHERE_MATCHES_ID = BaseColumns._ID + " = ?";

    private DBHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = DBHelper.getInstance(getContext());
        return true;
    }


    @Override
    public String getType(Uri uri) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case CONTACT:
            case SEARCH_PRE_QUERY:
            case SEARCH_QUERY:
                return ContactContract.CONTENT_TYPE;
            case CONTACT_ID:
                return ContactContract.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case CONTACT:
                break;
            case CONTACT_ID:
                throw new UnsupportedOperationException("Unable to insert by id. uri: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long id = -1;
        db.beginTransactionNonExclusive();
        try {
            id = db.insertWithOnConflict(ContactContract.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

        // notify change essentially indicates to any users with active cursors
        // that they need to "reload" the data
        notifyChange(uri);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        int match = uriMatcher.match(uri);
        String[] useProjection = projection;
        String useSelection = selection;
        String[] useSelectionArgs = selectionArgs;
        String limit = null;

        switch (match) {
            case CONTACT_ID:
                useSelection = WHERE_MATCHES_ID;
                useSelectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            case CONTACT:
                break;

            // Handle pre query. In this case we are rigging search to return no data as id 0
            // should not exist in a normal database.
            case SEARCH_PRE_QUERY:
                useSelection = WHERE_MATCHES_ID;
                useSelectionArgs = new String[]{"0"};
                break;

            // Handle search query. In this case, we are generating a custom projection since we know
            // the caller is the SearchView. Using aliases to map existing columns into the required
            // columns for search.
            case SEARCH_QUERY:
                useProjection = new String[]{ContactContract.Columns._ID,
                        ContactContract.Columns.NAME + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1,
                        ContactContract.Columns._ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA};
                // The search includes a query parameter named limit. (limit=50) The value may be used directly as the limit
                // for the query. ?limit=50.  You should honor it.
                limit = uri.getQueryParameter("limit");

                // Last segment should be search text. The selection args will find the text anywhere
                // in the name. Alternative would be to search from the beginning, etc.
                String queryText = uri.getLastPathSegment();
                useSelection = ContactContract.Columns.NAME + " like ?";
                useSelectionArgs = new String[]{"%" + queryText + "%"};
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor result = db.query(ContactContract.TABLE, useProjection, useSelection, useSelectionArgs, null, null, sortOrder, limit);
        // Register the cursor with the requested URI so the caller will receive
        // future database change notifications. Useful for "loaders" which take advantage
        // of this concept.
        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int match = uriMatcher.match(uri);
        String useSelection = selection;
        String[] useSelectionArgs = selectionArgs;
        switch (match) {
            case CONTACT_ID:
                useSelection = WHERE_MATCHES_ID;
                useSelectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            case CONTACT:
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = 0;
        db.beginTransactionNonExclusive();
        try {
            rows = db.update(ContactContract.TABLE, values, useSelection, useSelectionArgs);
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

        // notify change essentially indicates to any users with active cursors
        // that they need to "reload" the data
        notifyChange(uri);
        return rows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int match = uriMatcher.match(uri);
        String useSelection = selection;
        String[] useSelectionArgs = selectionArgs;
        switch (match) {
            case CONTACT_ID:
                useSelection = WHERE_MATCHES_ID;
                useSelectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            case CONTACT:
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Non Exclusive required for write ahead logging
        int rows = 0;
        db.beginTransactionNonExclusive();
        try {
            rows = db.delete(ContactContract.TABLE, useSelection, useSelectionArgs);
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

        // notify change essentially indicates to any users with active cursors
        // that they need to "reload" the data
        notifyChange(uri);
        return rows;
    }

    /**
     * Helper method to notify listeners of the changes to the database. Useful with loaders
     *
     * @param uri the URI for the content that changed.
     */
    private void notifyChange(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null, false);
    }
}
