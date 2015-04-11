package com.example.samplenetworking.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by jrogers on 2/24/15.
 */
public class CursorUtilities {

    public static void validateCursor(String error, Cursor valueCursor, ContentValues[] valuesArray) {
        assertNotNull("Null cursor. " + error, valueCursor);
        assertEquals("Unequal rows. " + error, valuesArray.length, valueCursor.getCount());

        int valueCount = 0;
        valueCursor.moveToPosition(-1); // ensure at beginning
        while (valueCursor.moveToNext()) {
            String offenseId = valueCursor.getString(valueCursor.getColumnIndex(Seattle911Contract.Columns.OFFENSE_ID));
            for (ContentValues values : valuesArray) {
                if (values.getAsString(Seattle911Contract.Columns.OFFENSE_ID).equals(offenseId)) {
                    ++valueCount;
                    validateCurrentRecord(error, valueCursor, values);
                    break;
                }
            }
        }
        assertEquals("Cursor doesn't contain values. " + error, valuesArray.length, valueCount);
    }

    public static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues, long id) {
        assertNotNull("Null cursor returned." + error, valueCursor);
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateId(error, valueCursor, id);
        validateCurrentRecord(error, valueCursor, expectedValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned in cursor. " + error, valueCursor.moveToNext());
    }

    private static void validateId(String error, Cursor valueCursor, long id) {
        int idx = valueCursor.getColumnIndex(BaseColumns._ID);
        assertFalse("Column '" + BaseColumns._ID + "' not found. " + error, idx == -1);
        assertEquals("Id does not match " + error, id, valueCursor.getLong(idx));
    }

    private static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            if (valueCursor.getType(idx) != Cursor.FIELD_TYPE_FLOAT) {
                assertEquals("Value '" + entry.getValue().toString() +
                        "' did not match the expected value '" +
                        expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
            }
            else {
                assertEquals("Value '" + entry.getValue().toString() +
                        "' did not match the expected value '" +
                        expectedValue + "'. " + error, expectedValue, Double.toString(valueCursor.getDouble(idx)));
            }
        }
    }
}
