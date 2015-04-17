package com.example.bchan92.homework1;

import android.content.ContentValues;

import com.example.bchan92.homework1.provider.CurrentConditionsContract;

public class DataUtilities {

    static ContentValues createInsertValues() {
        ContentValues values = new ContentValues();
        values.put(CurrentConditionsContract.Columns.CURRENT_CONDITION, "cloudy");
        values.put(CurrentConditionsContract.Columns.CURRENT_TEMP, "63");
        values.put(CurrentConditionsContract.Columns.HUMIDITY, "76");
        values.put(CurrentConditionsContract.Columns.WIND_SPEED, "8");
        values.put(CurrentConditionsContract.Columns.CITY_NAME, "Seattle");
        return values;
    }

    static ContentValues[] createBulkInsertValues() {
        ContentValues[] valuesArray = new ContentValues[2];
        valuesArray[0] = createInsertValues();
        ContentValues values = new ContentValues();

        values.put(CurrentConditionsContract.Columns.CURRENT_CONDITION, "sunny");
        values.put(CurrentConditionsContract.Columns.CURRENT_TEMP, "89");
        values.put(CurrentConditionsContract.Columns.HUMIDITY, "83");
        values.put(CurrentConditionsContract.Columns.WIND_SPEED, "2");
        values.put(CurrentConditionsContract.Columns.CITY_NAME, "Tacoma");

        valuesArray[1] = values;
        return valuesArray;
    }

    public static ContentValues[] mergeValues(ContentValues[] targetArray, ContentValues source) {
        for (ContentValues target: targetArray) {
            mergeValues(target, source);
        }
        return targetArray;
    }

    public static ContentValues mergeValues(ContentValues target, ContentValues source) {
        target.putAll(source);
        return target;
    }
}
