package com.example.joeroger.homework2.loader;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.joeroger.homework2.provider.DailyForecastContract;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;


public class DailyForecastLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_PROJECTION = "projection";
    private static final String ARG_SELECTION = "selection";
    private static final String ARG_SELECTION_ARGS = "selectionArgs";

    private static final String WHERE_MATCHES_CITY_MULTI_DAY = DailyForecastContract.Columns.CITY_ID + " = ?"
            + " AND (" + DailyForecastContract.Columns.DATA_TIME + " BETWEEN ? AND ?)";

    private static final String WHERE_MATCHES_CITY_AND_DAY = DailyForecastContract.Columns.CITY_ID + " = ?"
            + " AND (" + DailyForecastContract.Columns.DATA_TIME + " = ?)";

    private final WeakReference<OnDailyForecastLoaded> listenerRef;
    private final Context applicationContext;

    public interface OnDailyForecastLoaded {
        public void onDailyForecastLoaded(Cursor cursor);
    }

    public static void initLoader(LoaderManager loaderManager, Context context, OnDailyForecastLoaded listener, String[] projection, long cityId) {
        Bundle args = build5DayArgs(projection, cityId);
        loaderManager.initLoader(LoaderIds.DAILY_FORECAST_ID, args, new DailyForecastLoaderCallbacks(context, listener));
    }

    public static void restartLoader(LoaderManager loaderManager, Context context, OnDailyForecastLoaded listener, String[] projection, long cityId) {
        Bundle args = build5DayArgs(projection, cityId);
        loaderManager.restartLoader(LoaderIds.DAILY_FORECAST_ID, args, new DailyForecastLoaderCallbacks(context, listener));
    }

    public static void initLoader(LoaderManager loaderManager, Context context, OnDailyForecastLoaded listener, String[] projection, long cityId, long day) {
        Bundle args = buildSingleDayArgs(projection, cityId, day);
        loaderManager.initLoader(LoaderIds.DAILY_FORECAST_ID, args, new DailyForecastLoaderCallbacks(context, listener));
    }

    public static void restartLoader(LoaderManager loaderManager, Context context, OnDailyForecastLoaded listener, String[] projection, long cityId, long day) {
        Bundle args = buildSingleDayArgs(projection, cityId, day);
        loaderManager.restartLoader(LoaderIds.DAILY_FORECAST_ID, args, new DailyForecastLoaderCallbacks(context, listener));
    }

    private static Bundle buildSingleDayArgs(String[] projection, long cityId, long day) {
        Bundle args = new Bundle();
        args.putStringArray(ARG_PROJECTION, projection);

        String[] selectionArgs = new String[]{
                Long.toString(cityId),
                Long.toString(day)
        };

        args.putString(ARG_SELECTION, WHERE_MATCHES_CITY_AND_DAY);
        args.putStringArray(ARG_SELECTION_ARGS, selectionArgs);
        return args;
    }

    private static Bundle build5DayArgs(String[] projection, long cityId) {
        Bundle args = new Bundle();
        args.putStringArray(ARG_PROJECTION, projection);
        // Restrict to the 5 most current days
        Calendar now = Calendar.getInstance(TimeZone.getDefault());

        // If after 10pm, then show forecast for 5 days starting tomorrow.
        if (now.get(Calendar.HOUR_OF_DAY) > 21) {
            now.add(Calendar.DATE, 1);
        }
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);

        Calendar fiveDaysLater = Calendar.getInstance();
        fiveDaysLater.setTimeInMillis(now.getTimeInMillis());
        fiveDaysLater.add(Calendar.DATE, 5);

        String[] selectionArgs = new String[]{
                Long.toString(cityId),
                Long.toString(now.getTimeInMillis()),
                Long.toString(fiveDaysLater.getTimeInMillis())
        };

        args.putString(ARG_SELECTION, WHERE_MATCHES_CITY_MULTI_DAY);
        args.putStringArray(ARG_SELECTION_ARGS, selectionArgs);
        return args;
    }

    private DailyForecastLoaderCallbacks(Context context, OnDailyForecastLoaded listener) {
        this.applicationContext = context.getApplicationContext();
        listenerRef = new WeakReference<>(listener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(
                applicationContext,
                DailyForecastContract.URI,
                args.getStringArray(ARG_PROJECTION),
                args.getString(ARG_SELECTION),
                args.getStringArray(ARG_SELECTION_ARGS),
                DailyForecastContract.Columns.PERIOD);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        OnDailyForecastLoaded listener = listenerRef.get();
        if (listener != null) {
            listener.onDailyForecastLoaded(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        onLoadFinished(loader, null);
    }
}
