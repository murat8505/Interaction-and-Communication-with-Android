package com.example.joeroger.homework2.loader;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.joeroger.homework2.provider.ForecastContract;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by jrogers on 4/19/15.
 */
public class ForecastLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_PROJECTION = "projection";
    private static final String ARG_SELECTION = "selection";
    private static final String ARG_SELECTION_ARGS = "selectionArgs";

    private static final String WHERE_MATCHES_CITY_MULTI_HOUR = ForecastContract.Columns.CITY_ID + " = ?"
            + " AND (" + ForecastContract.Columns.DATA_TIME + " BETWEEN ? AND ?)";


    private final WeakReference<OnForecastLoaded> listenerRef;
    private final Context applicationContext;

    public interface OnForecastLoaded {
        public void onForecastLoaded(Cursor cursor);
    }

    public static void initLoader(LoaderManager loaderManager, Context context, OnForecastLoaded listener, String[] projection, long cityId, long day) {
        Bundle args = buildArgs(projection, cityId, day);
        loaderManager.initLoader(LoaderIds.FORECAST_ID, args, new ForecastLoaderCallbacks(context, listener));
    }

    public static void restartLoader(LoaderManager loaderManager, Context context, OnForecastLoaded listener, String[] projection, long cityId, long day) {
        Bundle args = buildArgs(projection, cityId, day);
        loaderManager.restartLoader(LoaderIds.FORECAST_ID, args, new ForecastLoaderCallbacks(context, listener));
    }

    private static Bundle buildArgs(String[] projection, long cityId, long day) {
        Bundle args = new Bundle();
        args.putStringArray(ARG_PROJECTION, projection);
        // Restrict to 24 hours
        Calendar startDay = Calendar.getInstance(TimeZone.getDefault());
        startDay.setTimeInMillis(day);
        startDay.set(Calendar.HOUR_OF_DAY, 0);
        startDay.set(Calendar.MINUTE, 0);
        startDay.set(Calendar.SECOND, 1);
        if (startDay.getTimeInMillis() < System.currentTimeMillis()) {
            startDay.setTimeInMillis(System.currentTimeMillis());
        }

        // Stop at midnight...
        Calendar oneDayLater = Calendar.getInstance();
        oneDayLater.setTimeInMillis(startDay.getTimeInMillis());
        oneDayLater.add(Calendar.DATE, 1);
        oneDayLater.set(Calendar.HOUR_OF_DAY, 0);
        oneDayLater.set(Calendar.MINUTE, 0);
        oneDayLater.set(Calendar.SECOND, 0);

        String[] selectionArgs = new String[]{
                Long.toString(cityId),
                Long.toString(startDay.getTimeInMillis()),
                Long.toString(oneDayLater.getTimeInMillis())
        };

        args.putString(ARG_SELECTION, WHERE_MATCHES_CITY_MULTI_HOUR);
        args.putStringArray(ARG_SELECTION_ARGS, selectionArgs);
        return args;
    }

    private ForecastLoaderCallbacks(Context context, OnForecastLoaded listener) {
        this.applicationContext = context.getApplicationContext();
        listenerRef = new WeakReference<>(listener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(
                applicationContext,
                ForecastContract.URI,
                args.getStringArray(ARG_PROJECTION),
                args.getString(ARG_SELECTION),
                args.getStringArray(ARG_SELECTION_ARGS),
                ForecastContract.Columns.PERIOD);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        OnForecastLoaded listener = listenerRef.get();
        if (listener != null) {
            listener.onForecastLoaded(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        onLoadFinished(loader, null);
    }
}
