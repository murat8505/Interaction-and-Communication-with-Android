package com.example.bchan92.homework1.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ForecastProvider extends ContentProvider {

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int FORECAST = 1;
    private static final int FORECAST_ID = 2;

    static {
        uriMatcher.addURI(CurrentConditionsContract.AUTHORITY, null, FORECAST);
        uriMatcher.addURI(CurrentConditionsContract.AUTHORITY, "#", FORECAST_ID);
    }

    private static final String WHERE_MATCHES_ID = BaseColumns._ID + " = ?";

    private DBHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = DBHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int match = uriMatcher.match(uri);
        String useSelection = selection;
        String[] useSelectionArgs = selectionArgs;
        switch (match) {
            case FORECAST_ID:
                useSelection = WHERE_MATCHES_ID;
                useSelectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            case FORECAST:
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor result = db.query(ForecastContract.TABLE, projection, useSelection, useSelectionArgs, null, null, sortOrder);

        // Register the cursor with the requested URI so the caller will receive
        // future database change notifications. Useful for "loaders" which take advantage
        // of this concept.
        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    @Override
    public String getType(Uri uri) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case FORECAST:
                return ForecastContract.CONTENT_TYPE;
            case FORECAST_ID:
                return ForecastContract.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case FORECAST:
                break;
            case FORECAST_ID:
                throw new UnsupportedOperationException("Unable to insert by id. uri: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long id = -1;
        db.beginTransactionNonExclusive();
        try {
            id = db.insertWithOnConflict(ForecastContract.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
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
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        String useSelection = selection;
        String[] useSelectionArgs = selectionArgs;
        switch (match) {
            case FORECAST_ID:
                useSelection = WHERE_MATCHES_ID;
                useSelectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            case FORECAST:
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Non Exclusive required for write ahead logging
        int rows = 0;
        db.beginTransactionNonExclusive();
        try {
            rows = db.delete(ForecastContract.TABLE, useSelection, useSelectionArgs);
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
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        String useSelection = selection;
        String[] useSelectionArgs = selectionArgs;
        switch (match) {
            case FORECAST_ID:
                useSelection = WHERE_MATCHES_ID;
                useSelectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            case FORECAST:
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = 0;
        db.beginTransactionNonExclusive();
        try {
            rows = db.update(ForecastContract.TABLE, values, useSelection, useSelectionArgs);
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
