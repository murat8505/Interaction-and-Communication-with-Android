package com.example.joeroger.homework2.service;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.joeroger.homework2.BuildConfig;
import com.example.joeroger.homework2.network.CacheUtils;
import com.example.joeroger.homework2.network.CityListJsonParser;
import com.example.joeroger.homework2.network.ConditionsJsonParser;
import com.example.joeroger.homework2.network.DailyForecastJsonParser;
import com.example.joeroger.homework2.network.DataFetcher;
import com.example.joeroger.homework2.network.ForecastJsonParser;
import com.example.joeroger.homework2.provider.BaseContract;
import com.example.joeroger.homework2.provider.CityConditionsContract;
import com.example.joeroger.homework2.provider.DailyForecastContract;
import com.example.joeroger.homework2.provider.ForecastContract;
import com.example.joeroger.homework2.utils.ConnectivityUtils;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Helper class to help various service classes load weather data
 */
/* package */ final class ServiceHelper {

    private static final String TAG = "ServiceHelper";
    private static final String[] AGE_PROJECTION = new String[]{"max(" + BaseContract.BaseWeatherColumns.UPDATED + ")"};
    private static final String[] CITY_PROJECTION = new String[]{CityConditionsContract.Columns.CITY_ID};
    private static final String[] CITY_STATE_PROJECTION = new String[]{
            CityConditionsContract.Columns.CURRENT,
            CityConditionsContract.Columns.FAVORITE
    };

    private static final ContentValues CLEAR_CURRENT_VALUE = new ContentValues(1);

    static {
        CLEAR_CURRENT_VALUE.put(CityConditionsContract.Columns.CURRENT, 0);
    }

    private static final String WHERE_CURRENT = CityConditionsContract.Columns.CURRENT + " != ?";
    private static final String[] WHERE_CURRENT_ARGS = new String[]{"0"};

    private static final String WHERE_NOT_CURRENT_OR_FAVORITE =
            CityConditionsContract.Columns.CURRENT + " = ? AND " + CityConditionsContract.Columns.FAVORITE + " = ?";
    private static final String[] WHERE_NOT_CURRENT_OR_FAVORITE_ARGS = new String[]{"0", "0"};

    private static final long TEN_MINUTES = 10 * 60 * 1000;
    private static final long THIRTY_MINUTES = 30 * 60 * 1000;
    private static final long TWO_HOURS = 2 * 60 * 60 * 1000;
    private static final long THREE_HOURS = 3 * 60 * 60 * 1000;
    private static final long CURRENT_CONDITION_PERIOD = THIRTY_MINUTES;
    private static final long FORECAST_PERIOD = THREE_HOURS;

    private static final CancellationCallback DEFAULT_CALLBACK = new CancellationCallback() {
        @Override
        public boolean isCancelled() {
            return false;
        }
    };

    interface CancellationCallback {
        boolean isCancelled();
    }

    final Context context;
    final CancellationCallback callback;

    /* package */ ServiceHelper(@NonNull Context context) {
        this(context, DEFAULT_CALLBACK);
    }

    /* package */ ServiceHelper(@NonNull Context context, @NonNull CancellationCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    /* package */ void purgeOldCities() {
        Cursor cursor = context.getContentResolver().query(CityConditionsContract.URI,
                CITY_PROJECTION,
                WHERE_NOT_CURRENT_OR_FAVORITE,
                WHERE_NOT_CURRENT_OR_FAVORITE_ARGS, null);

        try {
            while (cursor.moveToNext()) {
                long cityId = cursor.getLong(0);
                if (BuildConfig.DEBUG) Log.d(TAG, "Purging cityId: " + cityId);
                String[] selectionArgs = new String[]{Long.toString(cityId)};
                int rows = context.getContentResolver().delete(CityConditionsContract.URI, CityConditionsContract.WHERE_CITY_ID_MATCHES, selectionArgs);
                if (rows != 1) {
                    Log.w(TAG, "Purge failed");
                }
                context.getContentResolver().delete(ForecastContract.URI, CityConditionsContract.WHERE_CITY_ID_MATCHES, selectionArgs);
                context.getContentResolver().delete(DailyForecastContract.URI, CityConditionsContract.WHERE_CITY_ID_MATCHES, selectionArgs);
            }
        }
        finally {
            cursor.close();
        }
    }

    /* package */ void fetchData() {
        CacheUtils.initializeCache(context);

        if (!DataFetcher.ensureLatestSSL(context)) return;

        if (ConnectivityUtils.isNotConnected(context)) return;

        // Only refresh conditions if data is stale.
        if (dataValid(CityConditionsContract.URI, CURRENT_CONDITION_PERIOD, null, null)) {
            Log.d(TAG, "Current data has not expired");
            return;
        }

        long[] cityIds = getCityIds();
        int length = cityIds.length;
        if (length == 0) return;

        ArrayList<ContentValues> list = new ArrayList<>(cityIds.length);

        // If only one city, fetch using standard url
        if (cityIds.length == 1) {
            URL url = buildURL(new String[]{"weather"}, cityIds[0]);
            ContentValues[] newValues = DataFetcher.performGet(url, new ConditionsJsonParser());
            if (newValues != null) {
                list.add(augmentCityData(newValues[0]));
            }
        }
        // If multiple, fetch in bulk
        else {
            URL url = buildURL(cityIds);
            ContentValues[] newValues = DataFetcher.performGet(url, new CityListJsonParser());
            if (newValues != null) {
                for (ContentValues values : newValues) {
                    list.add(augmentCityData(values));
                }
            }
        }

        if (list.isEmpty()) return;

        ContentValues[] valuesList = new ContentValues[list.size()];
        list.toArray(valuesList);
        context.getContentResolver().bulkInsert(CityConditionsContract.URI, valuesList);
        if (callback.isCancelled()) return;

        loadForecastData(valuesList);

        if (BuildConfig.DEBUG) {
            CacheUtils.logCache();
        }
    }

    /* package */ boolean fetchNewLocation(LatLng location) {
        return fetchCityData(location, true, false);
    }

    /* package */ boolean fetchNewFavorite(LatLng location) {
        return fetchCityData(location, false, true);
    }

    private boolean fetchCityData(LatLng location, boolean isNewCurrent, boolean isNewFavorite) {
        CacheUtils.initializeCache(context);

        if (!DataFetcher.ensureLatestSSL(context)) return false;

        if (ConnectivityUtils.isNotConnected(context)) return false;

        URL url = buildURL(location);
        ContentValues[] newValues = DataFetcher.performGet(url, new CityListJsonParser());
        if (newValues == null || newValues.length == 0) return false;

        if (BuildConfig.DEBUG && newValues.length > 1) {
            Log.w(TAG, "API returned more than one city per row");
            return false;
        }

        ContentValues values = newValues[0];
        Integer cityId = values.getAsInteger(BaseContract.BaseWeatherColumns.CITY_ID);
        if (cityId == null || cityId == 0) {
            Log.w(TAG, "No city id was returned");
            return false;
        }

        augmentCityData(values);

        if (isNewCurrent) {
            Integer isCurrent = values.getAsInteger(CityConditionsContract.Columns.CURRENT);
            if (isCurrent != null && isCurrent == 1) {
                Log.d(TAG, "Already current location, so skip update");
                return true;
            }

            // Remove current city as current.
            context.getContentResolver().update(CityConditionsContract.URI, CLEAR_CURRENT_VALUE, WHERE_CURRENT, WHERE_CURRENT_ARGS);
            // Mark city as current
            values.put(CityConditionsContract.Columns.CURRENT, 1);
        }
        else if (isNewFavorite) {
            values.put(CityConditionsContract.Columns.FAVORITE, 1);
        }


        // Ensure data is updated next pass.
        values.put(CityConditionsContract.Columns.UPDATED, 0);
        context.getContentResolver().insert(CityConditionsContract.URI, values);
        if (callback.isCancelled()) return true;

        loadForecastData(newValues);

        if (BuildConfig.DEBUG) {
            CacheUtils.logCache();
        }

        return true;
    }

    private void loadForecastData(ContentValues[] valuesList) {
        // load forecast data for each city
        for (ContentValues data : valuesList) {
            long cityId = data.getAsLong(CityConditionsContract.Columns.CITY_ID);
            String[] selectionArgs = new String[]{Long.toString(cityId)};

            // The fetch icon code will fetch the icon if not already "fetched"
            DataFetcher.fetchIcon(context, data.getAsString(CityConditionsContract.Columns.ICON));

            if (callback.isCancelled()) return;

            // Update daily forecast less frequently as it doesn't change much
            if (!dataValid(DailyForecastContract.URI, FORECAST_PERIOD, CityConditionsContract.WHERE_CITY_ID_MATCHES, selectionArgs)) {
                loadDailyForecast(cityId);
            }

            if (callback.isCancelled()) return;

            // Update forecast less frequently as it doesn't change much
            if (!dataValid(ForecastContract.URI, FORECAST_PERIOD, CityConditionsContract.WHERE_CITY_ID_MATCHES, selectionArgs)) {
                loadForecast(cityId);
            }
        }
    }

    private void loadDailyForecast(long cityId) {
        URL url = buildURL(new String[]{"forecast", "daily"}, cityId);
        ContentValues[] values = DataFetcher.performGet(url, new DailyForecastJsonParser());
        if (callback.isCancelled()) return;
        int rows = 0;
        if (values != null) {
            rows = context.getContentResolver().bulkInsert(DailyForecastContract.URI, values);

            for (ContentValues data : values) {
                // The fetch icon code will fetch the icon if not already "fetched"
                DataFetcher.fetchIcon(context, data.getAsString(DailyForecastContract.Columns.ICON));
            }
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Inserted " + rows + " Daily Forecast Rows for city: " + cityId);
            CacheUtils.logCache();
        }
    }

    private void loadForecast(long cityId) {
        URL url = buildURL(new String[]{"forecast"}, cityId);
        ContentValues[] values = DataFetcher.performGet(url, new ForecastJsonParser());

        int rows = 0;
        if (values != null) {
            rows = context.getContentResolver().bulkInsert(ForecastContract.URI, values);

            for (ContentValues data : values) {
                // The fetch icon code will fetch the icon if not already "fetched"
                DataFetcher.fetchIcon(context, data.getAsString(ForecastContract.Columns.ICON));
            }
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Inserted " + rows + " Forecast Rows for city: " + cityId);
            CacheUtils.logCache();
        }
    }

    private long[] getCityIds() {
        Cursor cursor = null;
        long[] cityIds = null;

        try {
            cursor = context.getContentResolver().query(CityConditionsContract.URI, CITY_PROJECTION, null, null, null);
            cityIds = new long[cursor.getCount()];

            int i = 0;
            while (cursor.moveToNext()) {
                cityIds[i] = cursor.getLong(0);
                ++i;
            }
        }
        finally {
            if (cursor != null) cursor.close();
        }

        if (cityIds.length == 0) {
            //cityIds = new long[]{SEATTLE_CITY_ID};
        }

        return cityIds;
    }

    private static URL buildURL(String[] paths, long cityId) {
        if (cityId == 0) {
            Log.w(TAG, "Bad city id");
        }

        Uri.Builder builder = new Uri.Builder()
                .scheme("http")
                .authority("api.openweathermap.org")
                .appendPath("data")
                .appendPath("2.5");
        for (String path : paths) {
            builder.appendPath(path);
        }
        builder.appendQueryParameter("id", Long.toString(cityId))
                .appendQueryParameter("units", "imperial")
                .appendQueryParameter("APPID", BuildConfig.APP_ID);

        Uri uri = builder.build();

        return convertUri(uri);
    }

    private static URL buildURL(@NonNull long[] cityIds) {
        StringBuilder cityStringBuilder = new StringBuilder(256);
        boolean first = true;
        for (long cityId : cityIds) {
            if (!first) {
                cityStringBuilder.append(',');
            }
            cityStringBuilder.append(cityId);
            first = false;
        }

        Uri.Builder builder = new Uri.Builder()
                .scheme("http")
                .authority("api.openweathermap.org")
                .appendPath("data")
                .appendPath("2.5")
                .appendPath("group")
                .appendQueryParameter("id", cityStringBuilder.toString())
                .appendQueryParameter("units", "imperial")
                .appendQueryParameter("APPID", BuildConfig.APP_ID);

        Uri uri = builder.build();
        return convertUri(uri);
    }

    private static URL buildURL(@NonNull LatLng position) {

        Uri.Builder builder = new Uri.Builder()
                .scheme("http")
                .authority("api.openweathermap.org")
                .appendPath("data")
                .appendPath("2.5")
                .appendPath("find")
                .appendQueryParameter("lat", Double.toString(position.latitude))
                .appendQueryParameter("lon", Double.toString(position.longitude))
                .appendQueryParameter("cnt", "1")
                .appendQueryParameter("units", "imperial")
                .appendQueryParameter("APPID", BuildConfig.APP_ID);

        Uri uri = builder.build();

        return convertUri(uri);
    }

    @Nullable
    private static URL convertUri(Uri uri) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, uri.toString());
        }
        try {
            return new URL(uri.toString());
        }
        catch (IOException e) {
            Log.w(TAG, "Failed to build URL: " + uri.toString());
            return null;
        }
    }

    private ContentValues augmentCityData(ContentValues values) {
        Cursor cursor = null;

        String[] selectionArgs = new String[]{values.getAsString(CityConditionsContract.Columns.CITY_ID)};
        int currentPos = 0;
        int favoritePos = 1;

        try {
            cursor = context.getContentResolver().query(CityConditionsContract.URI,
                    CITY_STATE_PROJECTION, CityConditionsContract.WHERE_CITY_ID_MATCHES, selectionArgs, null);
            if (cursor.moveToFirst()) {
                values.put(CityConditionsContract.Columns.CURRENT, cursor.getInt(currentPos));
                values.put(CityConditionsContract.Columns.FAVORITE, cursor.getInt(favoritePos));
            }
        }
        finally {
            if (cursor != null) cursor.close();
        }

        return values;
    }

    private boolean dataValid(Uri uri, long ageMillis, String selection, String[] selectionArgs) {
        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(uri, AGE_PROJECTION, selection, selectionArgs, null);
            if (cursor.moveToFirst()) {
                long updateTime = cursor.isNull(0) ? 0 : cursor.getLong(0);
                return updateTime > System.currentTimeMillis() - ageMillis;
            }
        }
        finally {
            if (cursor != null) cursor.close();
        }

        return false;
    }
}
