package com.example.joeroger.homework2.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import java.util.List;

public class WeatherProvider extends ContentProvider {
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int ALL_ROWS = 1;
    private static final int ROW_BY_ID = 2;

    static {
        uriMatcher.addURI(BaseContract.AUTHORITY, "*", ALL_ROWS);
        uriMatcher.addURI(BaseContract.AUTHORITY, "*/#", ROW_BY_ID);
    }

    private static final String WHERE_MATCHES_ID = BaseColumns._ID + " = ?";

    private SQLiteOpenHelper dbHelper;


    public WeatherProvider() {
        super();
    }

    @Override
    public boolean onCreate() {
        dbHelper = DBHelper.getInstance(getContext());
        return true;
    }

    @Override
    public final void shutdown() {
        dbHelper.close();
        dbHelper = null;
    }

    @Override
    public String getType(Uri uri) {
        int match = uriMatcher.match(uri);
        List<String> segments = uri.getPathSegments();
        if (segments.isEmpty()) {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        String table = segments.get(0);
        switch (match) {
            case ALL_ROWS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + BaseContract.AUTHORITY + "/" + table;
            case ROW_BY_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + BaseContract.AUTHORITY + "/" + table;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = uriMatcher.match(uri);
        String table;
        switch (match) {
            case ALL_ROWS:
                table = uri.getLastPathSegment();
                break;
            case ROW_BY_ID:
                throw new UnsupportedOperationException("Unable to insert by id for uri: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        long id = -1;

        if (values.getAsLong(BaseContract.BaseWeatherColumns.UPDATED) == null) {
            values.put(BaseContract.BaseWeatherColumns.UPDATED, System.currentTimeMillis());
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransactionNonExclusive();
        try {
            id = db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

        if (id == -1) {
            return null;
        }

        // notify change essentially indicates to any users with active cursors
        // that they need to "reload" the data
        notifyChange(uri);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] valuesArray) {
        int match = uriMatcher.match(uri);
        String table;
        switch (match) {
            case ALL_ROWS:
                table = uri.getLastPathSegment();
                break;
            case ROW_BY_ID:
                throw new UnsupportedOperationException("Unable to insert by id for uri: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int count = 0;
        long updateTime = System.currentTimeMillis();

        db.beginTransactionNonExclusive();
        try {
            for (ContentValues values : valuesArray) {
                values.put(BaseContract.BaseWeatherColumns.UPDATED, updateTime);
                long id = db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (id != -1) {
                    ++count;
                }
            }
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

        // notify change essentially indicates to any users with active cursors
        // that they need to "reload" the data
        if (count > 0) {
            notifyChange(uri);
        }
        return count;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        int match = uriMatcher.match(uri);
        String table;
        String useSelection = selection;
        String[] useSelectionArgs = selectionArgs;
        switch (match) {
            case ALL_ROWS:
                table = uri.getLastPathSegment();
                break;
            case ROW_BY_ID:
                List<String> segments = uri.getPathSegments();
                table = segments.get(0);
                useSelection = WHERE_MATCHES_ID;
                useSelectionArgs = new String[]{segments.get(segments.size() - 1)};
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.acquireReference();

        try {
            Cursor cursor = db.query(table, projection, useSelection, useSelectionArgs, null, null, sortOrder);
            // Register the cursor with the requested URI so the caller will receive
            // future database change notifications. Useful for "loaders" which take advantage
            // of this concept.
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }
        finally {
            db.releaseReference();
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        String table;
        String useSelection = selection;
        String[] useSelectionArgs = selectionArgs;
        switch (match) {
            case ALL_ROWS:
                table = uri.getLastPathSegment();
                break;
            case ROW_BY_ID:
                List<String> segments = uri.getPathSegments();
                table = segments.get(0);
                useSelection = WHERE_MATCHES_ID;
                useSelectionArgs = new String[]{segments.get(segments.size() - 1)};
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        int rows = 0;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.acquireReference();

        db.beginTransactionNonExclusive();
        try {
            rows = db.update(table, values, useSelection, useSelectionArgs);
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

        // notify change essentially indicates to any users with active cursors
        // that they need to "reload" the data
        if (rows > 0) {
            notifyChange(uri);
        }
        return rows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        String table;
        String useSelection = selection;
        String[] useSelectionArgs = selectionArgs;
        switch (match) {
            case ALL_ROWS:
                table = uri.getLastPathSegment();
                break;
            case ROW_BY_ID:
                List<String> segments = uri.getPathSegments();
                table = segments.get(0);
                useSelection = WHERE_MATCHES_ID;
                useSelectionArgs = new String[]{segments.get(segments.size() - 1)};
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        int rows = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.acquireReference();
        db.beginTransactionNonExclusive();
        try {
            rows = db.delete(table, useSelection, useSelectionArgs);
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

        // notify change essentially indicates to any users with active cursors
        // that they need to "reload" the data
        if (rows > 0) {
            notifyChange(uri);
        }
        return rows;
    }

    private void notifyChange(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null, false);
    }
}
