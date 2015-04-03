package com.example.joeroger.samplesearchshare.provider;

import android.content.ContentValues;

import java.util.Calendar;

public class DataUtilities {

    static ContentValues createInsertContactValues() {
        ContentValues values = new ContentValues();
        values.put(ContactContract.Columns.NAME, "World Wide Web");
        values.put(ContactContract.Columns.ADDRESS, "Meyrin, Canton de Genève, Switzerland");
        Calendar birthDate = Calendar.getInstance();
        birthDate.set(1989, Calendar.MARCH, 12);
        values.put(ContactContract.Columns.BIRTH_DATE, birthDate.getTimeInMillis());
        return values;
    }

    static ContentValues[] createBulkInsertContactValues() {
        ContentValues[] valuesArray = new ContentValues[2];
        valuesArray[0] = createInsertContactValues();
        ContentValues values = new ContentValues();
        values.put(ContactContract.Columns.NAME, "Bob Smith");
        values.put(ContactContract.Columns.ADDRESS, "123 Demo St., Seattle, WA 98103");
        Calendar birthDate = Calendar.getInstance();
        birthDate.set(1993, Calendar.JANUARY, 5);
        values.put(ContactContract.Columns.BIRTH_DATE, birthDate.getTimeInMillis());
        valuesArray[1] = values;
        return valuesArray;
    }

    static ContentValues createUpdateBirthDate() {
        ContentValues values = new ContentValues();
        Calendar birthDate = Calendar.getInstance();
        birthDate.set(1991, Calendar.JANUARY, 5);
        values.put(ContactContract.Columns.BIRTH_DATE, birthDate.getTimeInMillis());
        return values;
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
