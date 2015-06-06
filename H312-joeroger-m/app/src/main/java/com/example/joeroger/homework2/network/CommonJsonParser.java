package com.example.joeroger.homework2.network;

import android.content.ContentValues;
import android.util.JsonReader;
import android.util.Log;

import com.example.joeroger.homework2.BuildConfig;
import com.example.joeroger.homework2.provider.BaseContract;
import com.example.joeroger.homework2.provider.CityConditionsContract;

import java.io.IOException;
import java.util.Comparator;

/**
 * Used to process common elements that appear in multiple feeds.
 */
/* package */ final class CommonJsonParser {

    /* package */
    static void processMainValues(JsonReader jsonReader, ContentValues values) throws IOException {
        jsonReader.beginObject();

        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case JsonConstants.Main.HUMIDITY:
                    values.put(CityConditionsContract.Columns.HUMIDITY, jsonReader.nextInt());
                    break;
                case JsonConstants.Main.PRESSURE:
                    // Convert pressure from hPA to inches of mercury (standard US form)
                    values.put(CityConditionsContract.Columns.PRESSURE, jsonReader.nextDouble() / JsonConstants.PRESSURE_CONVERSION);
                    break;
                case JsonConstants.Main.TEMPERATURE:
                    values.put(CityConditionsContract.Columns.TEMPERATURE, jsonReader.nextDouble());
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }

        jsonReader.endObject();
    }

    /* package */
    static void processWeatherValues(JsonReader jsonReader, ContentValues values) throws IOException {

        jsonReader.beginArray();
        int i = 0;

        // Loop over the array
        while (jsonReader.hasNext()) {

            jsonReader.beginObject();
            // Loop over the object
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();

                // Skip any secondary weather events. Never seen one, but we are in an array
                if (i != 0) {
                    jsonReader.skipValue();
                    continue;
                }

                // Process first weather event
                switch (name) {
                    case JsonConstants.Weather.CONDITION:
                        values.put(CityConditionsContract.Columns.CONDITION, jsonReader.nextString());
                        break;
                    case JsonConstants.Weather.ICON:
                        values.put(CityConditionsContract.Columns.ICON, jsonReader.nextString());
                        break;
                    default:
                        jsonReader.skipValue();
                        break;
                }
            }

            jsonReader.endObject();
            ++i; // increment object count
        }

        jsonReader.endArray();
    }

    /* package */
    static void processWindValues(JsonReader jsonReader, ContentValues values) throws IOException {
        jsonReader.beginObject();

        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case JsonConstants.Wind.WIND_DIRECTION:
                    // degrees to text N, NE, E, etc
                    values.put(CityConditionsContract.Columns.WIND_DIRECTION, convertWindDirection(jsonReader.nextDouble()));
                    break;
                case JsonConstants.Wind.WIND_SPEED:
                    // meters per sec to miles per hour
                    values.put(CityConditionsContract.Columns.WIND_SPEED, jsonReader.nextDouble()); //* JsonConstants.WIND_SPEED_CONVERSION);
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }

        jsonReader.endObject();

    }

    /* package */
    static ContentValues processCityValues(JsonReader jsonReader) throws IOException {
        ContentValues values = new ContentValues(2);
        jsonReader.beginObject();

        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case JsonConstants.City.CITY_ID:
                    values.put(BaseContract.BaseWeatherColumns.CITY_ID, jsonReader.nextLong());
                    break;
                case JsonConstants.City.CITY_NAME:
                    values.put(BaseContract.BaseWeatherColumns.CITY_NAME, jsonReader.nextString());
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }

        jsonReader.endObject();
        return values;
    }

    /* package */
    static String convertWindDirection(double degrees) {
        if (degrees <= 11.25) return "N";
        if (degrees <= 33.75) return "NNE";
        if (degrees <= 56.25) return "NE";
        if (degrees <= 78.75) return "ENE";
        if (degrees <= 101.25) return "E";
        if (degrees <= 123.75) return "ESE";
        if (degrees <= 146.25) return "SE";
        if (degrees <= 168.75) return "SSE";
        if (degrees <= 191.25) return "S";
        if (degrees <= 213.75) return "SSW";
        if (degrees <= 236.25) return "SW";
        if (degrees <= 258.75) return "WSW";
        if (degrees <= 281.25) return "W";
        if (degrees <= 303.75) return "WNW";
        if (degrees <= 326.25) return "NW";
        if (degrees <= 348.75) return "NNW";
        return "N";
    }

    /* package */
    static final Comparator<ContentValues> SORT_BY_TIME = new Comparator<ContentValues>() {
        @Override
        public int compare(ContentValues lhs, ContentValues rhs) {
            Long lhsTime = lhs.getAsLong(BaseContract.BaseWeatherColumns.DATA_TIME);
            Long rhsTime = rhs.getAsLong(BaseContract.BaseWeatherColumns.DATA_TIME);
            return lhsTime < rhsTime ? -1 : (lhs == rhs ? 0 : 1);
        }
    };

    /* package */
    static void closeReader(String TAG, JsonReader jsonReader) {
        try {
            if (jsonReader != null) jsonReader.close();
        }
        catch (IOException e) {
            logIOException(TAG, e);
        }
    }

    /* package */
    static void logIOException(String TAG, IOException e) {
        Log.w(TAG, "Unexpected exception: " + e.getMessage());
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Stack Trace", e);
        }
    }

}


