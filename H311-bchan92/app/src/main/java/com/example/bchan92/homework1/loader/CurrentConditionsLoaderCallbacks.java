package com.example.bchan92.homework1.loader;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.bchan92.homework1.provider.CurrentConditionsContract;

import java.lang.ref.WeakReference;

public final class CurrentConditionsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "CurrentConditionsLoaderCB";

    private static final String ARG_URI = "uri";
    private static final String ARG_PROJECTION = "projection";

    private final Context applicationContext;
    private final WeakReference<ContactLoadListener> listenerRef;

    public interface ContactLoadListener {
        public void onContactLoadComplete(Cursor cursor);
    }

    /**
     * Initialize a loader. Call from an Activity's onCreate or a Fragment's onActivityCreated method.
     */
    public static void initLoader(Context context, LoaderManager loaderManager, ContactLoadListener listener, String[] projection) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, CurrentConditionsContract.URI);
        args.putStringArray(ARG_PROJECTION, projection);
        loaderManager.initLoader(LoaderIds.CURRENT_CONDITIONS_LOADER,
                args, new CurrentConditionsLoaderCallbacks(context, listener));

    }

    /**
     * Initialize a loader to query a specific city using the provided city id.
     */
    public static void initLoader(Context context, LoaderManager loaderManager, ContactLoadListener listener, String[] projection, long cityId) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, ContentUris.withAppendedId(CurrentConditionsContract.URI, cityId));
        args.putStringArray(ARG_PROJECTION, projection);
        loaderManager.initLoader(LoaderIds.CURRENT_CONDITIONS_LOADER,
                args, new CurrentConditionsLoaderCallbacks(context, listener));
    }

    /**
     * Private constructor to prevent direct instantiation
     *
     * @param context  The context to use for starting the loader
     * @param listener The listener to use to provide city cursor data back to the caller.
     */
    private CurrentConditionsLoaderCallbacks(Context context, ContactLoadListener listener) {
        applicationContext = context.getApplicationContext();
        listenerRef = new WeakReference<>(listener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        Log.d(TAG, "onCreateLoader");
        Uri uri = bundle.getParcelable(ARG_URI);

        return new CursorLoader(
                applicationContext,
                uri,
                bundle.getStringArray(ARG_PROJECTION),
                null,
                null,
                CurrentConditionsContract.Columns.CITY_NAME + " asc, " + CurrentConditionsContract.Columns._ID + " asc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished result is " + (cursor != null ? "not null" : "null"));

        // Have a result. Note, it may be null, but we want to tell our listener if
        // we still have one with the value regardless.
        ContactLoadListener listener = listenerRef.get();
        if (listener != null) {
            Log.d(TAG, "onLoadFinished. Notifying listener");
            listener.onContactLoadComplete(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.d(TAG, "onLoadReset");
        onLoadFinished(cursorLoader, null);
    }
}
