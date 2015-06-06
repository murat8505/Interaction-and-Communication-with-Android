package com.example.joeroger.homework1.provider;

import android.content.ContentValues;


public class DataUtilities {
    static ContentValues createCityConditionValues() {
        ContentValues values = new ContentValues();
        values.put(CityConditionsContract.Columns.CITY_ID, 5809844);
        values.put(CityConditionsContract.Columns.CITY_NAME, "Seattle");
        values.put(CityConditionsContract.Columns.ICON, "01n");
        values.put(CityConditionsContract.Columns.CONDITION, "Clear");
        values.put(CityConditionsContract.Columns.TEMPERATURE, 48.13);
        values.put(CityConditionsContract.Columns.HUMIDITY, 70);
        values.put(CityConditionsContract.Columns.PRESSURE, 1024.89);
        values.put(CityConditionsContract.Columns.WIND_SPEED, 5.96);
        values.put(CityConditionsContract.Columns.WIND_DIRECTION, "NNE");
        values.put(CityConditionsContract.Columns.DATA_TIME, 1429246948000L);
        return values;
    }

    static ContentValues createCityConditionReplaceValues() {
        ContentValues values = new ContentValues();
        values.put(CityConditionsContract.Columns.CITY_ID, 5809844);
        values.put(CityConditionsContract.Columns.CITY_NAME, "Seattle");
        values.put(CityConditionsContract.Columns.ICON, "01n");
        values.put(CityConditionsContract.Columns.CONDITION, "Rainy");
        values.put(CityConditionsContract.Columns.TEMPERATURE, 47.13);
        values.put(CityConditionsContract.Columns.HUMIDITY, 60);
        values.put(CityConditionsContract.Columns.PRESSURE, 1018.89);
        values.put(CityConditionsContract.Columns.WIND_SPEED, 7.96);
        values.put(CityConditionsContract.Columns.WIND_DIRECTION, "E");
        values.put(CityConditionsContract.Columns.DATA_TIME, 1439246948000L);
        return values;
    }

    public static ContentValues[] mergeValues(ContentValues[] targetArray, ContentValues source) {
        for (ContentValues target : targetArray) {
            mergeValues(target, source);
        }
        return targetArray;
    }

    public static ContentValues mergeValues(ContentValues target, ContentValues source) {
        target.putAll(source);
        return target;
    }
}
