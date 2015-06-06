package com.example.joeroger.homework1.loader;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.joeroger.homework1.provider.CityConditionsContract;

import java.lang.ref.WeakReference;

/**
 * Created by jrogers on 4/18/15.
 */
public class CityConditionsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_PROJECTION = "projection";
    private static final String ARG_SELECTION = "selection";
    private static final String ARG_SELECTION_ARGS = "selectionArgs";

    private static final String WHERE_MATCHES_CITY_ID = CityConditionsContract.Columns.CITY_ID + " = ?";

    private final Context applicationContext;
    private final WeakReference<OnCityConditionsLoaded> listenerRef;

    public interface OnCityConditionsLoaded {
        public void onCityConditionsLoaded(Cursor cursor);
    }

    public static void initLoader(LoaderManager loaderManager, Context context, OnCityConditionsLoaded listener, String[] projection, long cityId) {
        Bundle args = buildArgs(projection, cityId);
        loaderManager.initLoader(LoaderIds.CITY_CONDITIONS_ID, args, new CityConditionsLoaderCallbacks(context, listener));
    }

    public static void restartLoader(LoaderManager loaderManager, Context context, OnCityConditionsLoaded listener, String[] projection, long cityId) {
        Bundle args = buildArgs(projection, cityId);
        loaderManager.restartLoader(LoaderIds.CITY_CONDITIONS_ID, args, new CityConditionsLoaderCallbacks(context, listener));
    }

    private static Bundle buildArgs(String[] projection, long cityId) {
        Bundle args = new Bundle();
        args.putStringArray(ARG_PROJECTION, projection);
        args.putString(ARG_SELECTION, WHERE_MATCHES_CITY_ID);
        String[] selectionArgs = new String[] { Long.toString(cityId) };
        args.putStringArray(ARG_SELECTION_ARGS, selectionArgs);
        return args;
    }

    private CityConditionsLoaderCallbacks(Context context, OnCityConditionsLoaded listener) {
        applicationContext = context.getApplicationContext();
        listenerRef = new WeakReference<>(listener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                applicationContext,
                CityConditionsContract.URI,
                args.getStringArray(ARG_PROJECTION),
                args.getString(ARG_SELECTION),
                args.getStringArray(ARG_SELECTION_ARGS),
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        OnCityConditionsLoaded listener = listenerRef.get();
        if (listener != null) {
            listener.onCityConditionsLoaded(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        onLoadFinished(loader, null);
    }
}
