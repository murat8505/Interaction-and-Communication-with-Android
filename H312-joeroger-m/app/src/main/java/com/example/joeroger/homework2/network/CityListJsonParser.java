package com.example.joeroger.homework2.network;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;

import com.example.joeroger.homework2.provider.ForecastContract;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by jrogers on 5/17/15.
 */
public class CityListJsonParser implements DataFetcher.DataConverter {
    private static final String TAG = "FindCityJsonParser";

    @Nullable
    @Override
    public ContentValues[] convertData(@NonNull InputStream inputStream) {
        JsonReader jsonReader = null;
        try {
            jsonReader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            return processCityData(jsonReader);
        }
        catch (IOException e) {
            CommonJsonParser.logIOException(TAG, e);
            return null;
        }
        finally {
            CommonJsonParser.closeReader(TAG, jsonReader);
        }
    }

    @Nullable
    private ContentValues[] processCityData(JsonReader jsonReader) throws IOException {
        ContentValues[] cityValues = null;

        jsonReader.beginObject();

        int count = 1; // should be only one...
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case JsonConstants.Forecast.COUNT:
                    count = jsonReader.nextInt();
                    break;
                case JsonConstants.Forecast.LIST:
                    cityValues = processCityList(jsonReader, count);
                    break;
                default:
                    jsonReader.skipValue();
            }
        }

        jsonReader.endObject();

        if (cityValues == null) {
            Log.w(TAG, "Failed to load cities");
            return null;
        }

        return cityValues;
    }

    private ContentValues[] processCityList(JsonReader jsonReader, int count) throws IOException {
        ArrayList<ContentValues> list = new ArrayList<>(count);

        jsonReader.beginArray();

        while (jsonReader.hasNext()) {
            // Innards are just conditions...
            list.add(ConditionsJsonParser.processConditionsObject(jsonReader));
        }

        jsonReader.endArray();

        ContentValues[] result = new ContentValues[list.size()];
        return list.toArray(result);
    }
}
