package com.example.samplenetworking.loader;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.samplenetworking.provider.Seattle911Contract;

import java.lang.ref.WeakReference;

/**
 * Created by jrogers on 4/6/15.
 */
public class IncidentLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_URI = "Uri";
    private static final String ARG_PROJECTION = "projection";


    public interface OnIncidentLoaderListener {

        public void onIncidentLoadComplete(@Nullable Cursor cursor);
    }

    private final Context applicationContext;
    private final WeakReference<OnIncidentLoaderListener> listenerRef;

    public static void initLoader(Context context, LoaderManager loaderManager, OnIncidentLoaderListener listener, String[] projection) {
        initLoader(context, loaderManager, listener, Seattle911Contract.NO_INCIDENT_ID, projection);
    }

    public static void initLoader(Context context, LoaderManager loaderManager, OnIncidentLoaderListener listener, long id, String[] projection) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, id == Seattle911Contract.NO_INCIDENT_ID ?
                Seattle911Contract.URI :
                ContentUris.withAppendedId(Seattle911Contract.URI, id));
        args.putStringArray(ARG_PROJECTION, projection);
        loaderManager.initLoader(LoaderIds.INCIDENT_LOADER, args, new IncidentLoaderCallbacks(context, listener));
    }

    private IncidentLoaderCallbacks(Context context, OnIncidentLoaderListener listener) {
        applicationContext = context.getApplicationContext();
        listenerRef = new WeakReference<>(listener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        Uri uri = args.getParcelable(ARG_URI);
        return new CursorLoader(applicationContext,
                uri,
                args.getStringArray(ARG_PROJECTION),
                null,
                null,
                Seattle911Contract.Columns.CLEARANCE_DATE + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        OnIncidentLoaderListener listener = listenerRef.get();
        if (listener != null) {
            listener.onIncidentLoadComplete(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        onLoadFinished(loader, null);
    }
}