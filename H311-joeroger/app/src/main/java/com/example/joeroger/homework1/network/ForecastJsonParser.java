package com.example.joeroger.homework1.network;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;

import com.example.joeroger.homework1.provider.ForecastContract;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;


public final class ForecastJsonParser implements DataFetcher.DataConverter {

    private static final String TAG = "ForecastJsonParser";

    @Nullable
    @Override
    public ContentValues[] convertData(@NonNull InputStream inputStream) {
        JsonReader jsonReader = null;
        try {
            jsonReader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            return processForecastData(jsonReader);
        }
        catch (IOException e) {
            CommonJsonParser.logIOException(TAG, e);
            return null;
        }
        finally {
            CommonJsonParser.closeReader(TAG, jsonReader);
        }
    }

    private ContentValues[] processForecastData(JsonReader jsonReader) throws IOException {
        ContentValues[] forecastValues = null;
        ContentValues cityValues = null;

        jsonReader.beginObject();

        int count = 48; // 6days 8 periods which should be 8 periods more than necessary.
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
                    forecastValues = processForecastList(jsonReader, count);
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

    private ContentValues[] processForecastList(JsonReader jsonReader, int count) throws IOException {
        ArrayList<ContentValues> list = new ArrayList<>(count);

        jsonReader.beginArray();

        while (jsonReader.hasNext()) {
            // Innards are just conditions for every 3 hours...
            list.add(ConditionsJsonParser.processConditionsObject(jsonReader));
        }

        jsonReader.endArray();

        // Ensure sorted in order of time ascending.
        Collections.sort(list, CommonJsonParser.SORT_BY_TIME);
        ContentValues[] result = new ContentValues[list.size()];
        return list.toArray(result);
    }
}
