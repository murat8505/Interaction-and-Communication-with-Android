package com.example.joeroger.homework2.network;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.JsonReader;

import com.example.joeroger.homework2.provider.CityConditionsContract;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Parser for current conditions JSON
 */
public final class ConditionsJsonParser implements DataFetcher.DataConverter {
    private static final String TAG = "ConditionsJsonParser";

    @Nullable
    @Override
    public ContentValues[] convertData(@NonNull InputStream inputStream) {

        JsonReader jsonReader = null;
        try {
            jsonReader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            return new ContentValues[] { processConditionsObject(jsonReader) };
        }
        catch(IOException e) {
            CommonJsonParser.logIOException(TAG, e);
            return null;
        }
        finally {
            CommonJsonParser.closeReader(TAG, jsonReader);
        }
    }

    /* package */ static ContentValues processConditionsObject(JsonReader jsonReader) throws IOException {
        ContentValues values = new ContentValues();

        jsonReader.beginObject();

        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case JsonConstants.Conditions.CITY_ID:
                    values.put(CityConditionsContract.Columns.CITY_ID, jsonReader.nextLong());
                    break;
                case JsonConstants.Conditions.CITY_NAME:
                    values.put(CityConditionsContract.Columns.CITY_NAME, jsonReader.nextString());
                    break;
                case JsonConstants.Conditions.DATA_TIME:
                    // Time in seconds UTC, convert to milliseconds
                    values.put(CityConditionsContract.Columns.DATA_TIME, jsonReader.nextLong() * 1000);
                    break;
                case JsonConstants.Conditions.MAIN:
                    CommonJsonParser.processMainValues(jsonReader, values);
                    break;
                case JsonConstants.Conditions.WEATHER:
                    CommonJsonParser.processWeatherValues(jsonReader, values);
                    break;
                case JsonConstants.Conditions.WIND:
                    CommonJsonParser.processWindValues(jsonReader, values);
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }

        jsonReader.endObject();
        return values;
    }
}
