package com.example.joeroger.homework1.network;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;

import com.example.joeroger.homework1.provider.CityConditionsContract;
import com.example.joeroger.homework1.provider.DailyForecastContract;
import com.example.joeroger.homework1.provider.ForecastContract;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public final class DailyForecastJsonParser implements DataFetcher.DataConverter {

    private static final String TAG = "DailyForecastJsonParser";

    @Nullable
    @Override
    public ContentValues[] convertData(@NonNull InputStream inputStream) {
        JsonReader jsonReader = null;
        try {
            jsonReader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            return processDailyForecastData(jsonReader);
        }
        catch (IOException e) {
            CommonJsonParser.logIOException(TAG, e);
            return null;
        }
        finally {
            CommonJsonParser.closeReader(TAG, jsonReader);
        }
    }

    private ContentValues[] processDailyForecastData(JsonReader jsonReader) throws IOException {
        ContentValues[] forecastValues = null;
        ContentValues cityValues = null;

        jsonReader.beginObject();

        int count = 7; // default is 7 days
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case JsonConstants.Forecast.CITY:
                    cityValues = CommonJsonParser.processCityValues(jsonReader);
                    break;
                case JsonConstants.Forecast.COUNT:
                    count = jsonReader.nextInt();
                    break;
                case JsonConstants.Forecast.LIST:
                    forecastValues = processDailyForecastList(jsonReader, count);
                    break;
                default:
                    jsonReader.skipValue();
            }
        }

        jsonReader.endObject();

        if (forecastValues == null || cityValues == null) {
            Log.w(TAG, "Failed to load forecast");
            return null;
        }

        int period = 0;
        // Merge city data into each row...
        for (ContentValues values : forecastValues) {
            values.put(ForecastContract.Columns.PERIOD, period);
            values.putAll(cityValues);
            ++period;
        }

        return forecastValues;
    }

    private ContentValues[] processDailyForecastList(JsonReader jsonReader, int count) throws IOException {
        ArrayList<ContentValues> list = new ArrayList<>(count);

        jsonReader.beginArray();

        while (jsonReader.hasNext()) {
            list.add(processDailyForecast(jsonReader));
        }

        jsonReader.endArray();

        // Ensure sorted in order of time ascending.
        Collections.sort(list, CommonJsonParser.SORT_BY_TIME);
        ContentValues[] result = new ContentValues[list.size()];
        return list.toArray(result);
    }

    private ContentValues processDailyForecast(JsonReader jsonReader) throws IOException {
        ContentValues values = new ContentValues();

        jsonReader.beginObject();

        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();

            switch (name) {
                case JsonConstants.DailyForecast.DATA_TIME:
                    // Time in seconds UTC, convert to milliseconds
                    values.put(CityConditionsContract.Columns.DATA_TIME, jsonReader.nextLong() * 1000);
                    break;
                case JsonConstants.DailyForecast.TEMP:
                    processTempValues(jsonReader, values);
                    break;
                case JsonConstants.DailyForecast.WEATHER:
                    CommonJsonParser.processWeatherValues(jsonReader, values);
                    break;
                case JsonConstants.DailyForecast.HUMIDITY:
                    values.put(DailyForecastContract.Columns.HUMIDITY, jsonReader.nextInt());
                    break;
                case JsonConstants.DailyForecast.PRESSURE:
                    // Convert pressure from hPA to inches of mercury (standard US form)
                    values.put(DailyForecastContract.Columns.PRESSURE, jsonReader.nextDouble() / JsonConstants.PRESSURE_CONVERSION);
                    break;
                case JsonConstants.DailyForecast.WIND_DIRECTION:
                    // degrees to text N, NE, E, etc
                    values.put(DailyForecastContract.Columns.WIND_DIRECTION, CommonJsonParser.convertWindDirection(jsonReader.nextDouble()));
                    break;
                case JsonConstants.DailyForecast.WIND_SPEED:
                    // meters per sec to miles per hour
                    values.put(DailyForecastContract.Columns.WIND_SPEED, jsonReader.nextDouble() * JsonConstants.WIND_SPEED_CONVERSION);
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }
        jsonReader.endObject();

        return values;
    }

    private void processTempValues(JsonReader jsonReader, ContentValues values) throws IOException {
        jsonReader.beginObject();

        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();

            switch (name) {
                case JsonConstants.Temp.MAX_TEMP:
                    values.put(DailyForecastContract.Columns.MAX_TEMP, jsonReader.nextDouble());
                    break;
                case JsonConstants.Temp.MIN_TEMP:
                    values.put(DailyForecastContract.Columns.MIN_TEMP, jsonReader.nextDouble());
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }

        jsonReader.endObject();
    }
}
