package com.example.joeroger.samplelocation.provider;

import android.content.ContentValues;

public class DataUtilities {

    static ContentValues createInsertValues() {
        ContentValues values = new ContentValues();
        values.put(Seattle911Contract.Columns.OFFENSE_ID, 2015109316);
        values.put(Seattle911Contract.Columns.CATEGORY, "DISTURBANCES");
        values.put(Seattle911Contract.Columns.SUB_CATEGORY, "No DISTURBANCES");
        values.put(Seattle911Contract.Columns.CLEARANCE_DATE, System.currentTimeMillis());
        values.put(Seattle911Contract.Columns.DESCRIPTION, "NOISE DISTURBANCE");
        values.put(Seattle911Contract.Columns.LOCATION, "47XX BLOCK OF 18 AV NE");
        values.put(Seattle911Contract.Columns.LONGITUDE, -122.308340727d);
        values.put(Seattle911Contract.Columns.LATITUDE, 47.663962693d);
        return values;
    }

    static ContentValues[] createBulkInsertValues() {
        ContentValues[] valuesArray = new ContentValues[2];
        valuesArray[0] = createInsertValues();
        ContentValues values = new ContentValues();
        values.put(Seattle911Contract.Columns.OFFENSE_ID, 2015109273);
        values.put(Seattle911Contract.Columns.CATEGORY, "SUSPICIOUS CIRCUMSTANCES");
        values.put(Seattle911Contract.Columns.SUB_CATEGORY, "SUSPICIOUS CIRCUMSTANCES");
        values.put(Seattle911Contract.Columns.CLEARANCE_DATE, System.currentTimeMillis());
        values.put(Seattle911Contract.Columns.DESCRIPTION, "SUSPICIOUS PERSON");
        values.put(Seattle911Contract.Columns.LOCATION, "47XX BLOCK OF 20 AV NE");
        values.put(Seattle911Contract.Columns.LONGITUDE, -122.306511946d);
        values.put(Seattle911Contract.Columns.LATITUDE, 47.663965126);
        valuesArray[1] = values;
        return valuesArray;
    }

    static ContentValues createUpdateValues() {
        ContentValues values = new ContentValues();
        values.put(Seattle911Contract.Columns.SUB_CATEGORY, "DISTURBANCES");
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
