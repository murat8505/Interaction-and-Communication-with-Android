package com.example.joeroger.homework1.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.joeroger.BuildConfig;
import com.example.joeroger.homework1.network.CacheUtils;
import com.example.joeroger.homework1.network.ConditionsJsonParser;
import com.example.joeroger.homework1.network.DailyForecastJsonParser;
import com.example.joeroger.homework1.network.DataFetcher;
import com.example.joeroger.homework1.network.ForecastJsonParser;
import com.example.joeroger.homework1.provider.BaseContract;
import com.example.joeroger.homework1.provider.CityConditionsContract;
import com.example.joeroger.homework1.provider.DailyForecastContract;
import com.example.joeroger.homework1.provider.ForecastContract;

import java.io.IOException;
import java.net.URL;

public class WeatherService extends IntentService {
    private static final String TAG = "WeatherService";

    private static final String ACTION_LOAD_WEATHER = BuildConfig.APPLICATION_ID + ".service.action.LOAD_WEATHER";
    private static final String ACTION_LOAD_FORECAST = BuildConfig.APPLICATION_ID + ".service.action.LOAD_FORECAST";
    private static final String ACTION_LOAD_DAILY_FORECAST = BuildConfig.APPLICATION_ID + ".service.action.LOAD_DAILY_FORECAST";

    private static final String EXTRA_CITY_ID = BuildConfig.APPLICATION_ID + ".service.extra.CITY_ID";

    private static final String[] AGE_PROJECTION = new String[]{"max(" + BaseContract.BaseWeatherColumns.UPDATED + ")"};
    private static final String WHERE_CITY_ID_MATCHES = BaseContract.BaseWeatherColumns.CITY_ID + " = ?";
    private static final long TEN_MINUTES = 10 * 60 * 1000;
    private static final long SIX_HOURS = 6 * 60 * 60 * 1000;

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionLoad(Context context) {
        Intent intent = new Intent(context, WeatherService.class);
        intent.setAction(ACTION_LOAD_WEATHER);
        context.startService(intent);
    }

    public static void startActionLoadForecast(Context context, long cityId) {
        Intent intent = new Intent(context, WeatherService.class);
        intent.setAction(ACTION_LOAD_FORECAST);
        intent.putExtra(EXTRA_CITY_ID, cityId);
        context.startService(intent);
    }

    public static void startActionLoadDailyForecast(Context context, long cityId) {
        Intent intent = new Intent(context, WeatherService.class);
        intent.setAction(ACTION_LOAD_DAILY_FORECAST);
        intent.putExtra(EXTRA_CITY_ID, cityId);
        context.startService(intent);
    }

    public WeatherService() {
        super("WeatherService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_LOAD_WEATHER:
                    handleActionLoadWeather();
                    break;
                case ACTION_LOAD_FORECAST:
                    handleActionLoadForecast(intent.getLongExtra(EXTRA_CITY_ID, 0));
                    break;
                case ACTION_LOAD_DAILY_FORECAST:
                    handleActionLoadDailyForecast(intent.getLongExtra(EXTRA_CITY_ID, 0));
                    break;
                default:
                    break;
            }
        }
    }

    private void handleActionLoadWeather() {
        CacheUtils.initializeCache(this);

        if (!DataFetcher.ensureLatestSSL(this)) return;

        // Only refresh conditions every ten minutes max
        if (dataValid(CityConditionsContract.URI, TEN_MINUTES, null, null)) return;

        // Hardcoding Seattle for HW1
        URL url = buildURL(new String[]{"weather"}, 5809844);
        ContentValues[] values = DataFetcher.performGet(url, new ConditionsJsonParser());
        if (values != null) {
            getContentResolver().bulkInsert(CityConditionsContract.URI, values);

            // Queue up forecast work
            for (ContentValues data : values) {
                long cityId = data.getAsLong(CityConditionsContract.Columns.CITY_ID);
                String[] selectionArgs = new String[]{Long.toString(cityId)};

                // Update daily forecast every twelve hours. Not changing much...
                if (!dataValid(DailyForecastContract.URI, SIX_HOURS, WHERE_CITY_ID_MATCHES, selectionArgs)) {
                    startActionLoadDailyForecast(this, cityId);
                }

                // Update forecast every six hours to maintain future periods...
                if (!dataValid(ForecastContract.URI, SIX_HOURS, WHERE_CITY_ID_MATCHES, selectionArgs)) {
                    startActionLoadForecast(this, cityId);
                }
            }
        }

        if (BuildConfig.DEBUG) {
            CacheUtils.logCache();
        }
    }

    private void handleActionLoadForecast(long cityId) {
        CacheUtils.initializeCache(this);

        if (!DataFetcher.ensureLatestSSL(this)) return;

        URL url = buildURL(new String[]{"forecast"}, cityId);
        ContentValues[] values = DataFetcher.performGet(url, new ForecastJsonParser());
        int rows = 0;
        if (values != null) {
            rows = getContentResolver().bulkInsert(ForecastContract.URI, values);
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Inserted " + rows + " Forecast Rows for city: " + cityId);
            CacheUtils.logCache();
        }
    }

    private void handleActionLoadDailyForecast(long cityId) {
        CacheUtils.initializeCache(this);

        if (!DataFetcher.ensureLatestSSL(this)) return;

        URL url = buildURL(new String[]{"forecast", "daily"}, cityId);
        ContentValues[] values = DataFetcher.performGet(url, new DailyForecastJsonParser());
        int rows = 0;
        if (values != null) {
            rows = getContentResolver().bulkInsert(DailyForecastContract.URI, values);

        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Inserted " + rows + " Daily Forecast Rows for city: " + cityId);
            CacheUtils.logCache();
        }
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

    private boolean dataValid(Uri uri, long ageMillis, String selection, String[] selectionArgs) {
        Cursor cursor = null;

        try {
            cursor = getContentResolver().query(uri, AGE_PROJECTION, selection, selectionArgs, null);
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
