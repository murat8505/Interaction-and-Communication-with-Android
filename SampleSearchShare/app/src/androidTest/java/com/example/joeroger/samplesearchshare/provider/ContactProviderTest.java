package com.example.joeroger.samplesearchshare.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

public class ContactProviderTest extends AndroidTestCase {

    public void setUp() throws Exception {
        super.setUp();
        cleanupDB();
    }

    private void cleanupDB() {
        DBHelper helper = DBHelper.getInstance(getContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(ContactContract.TABLE, null, null);
        db.close();
    }

    public void testGetType() throws Exception {
        // Verify the right type is returned using the standard URI
        String type = getContext().getContentResolver().getType(ContactContract.URI);
        assertEquals(ContactContract.CONTENT_TYPE, type);

        // Verify the right type is returned using a URI with an _ID attached.
        type = getContext().getContentResolver().getType(ContentUris.withAppendedId(ContactContract.URI, 1));
        assertEquals(ContactContract.CONTENT_ITEM_TYPE, type);

        // Verify a bad uri
        type = getContext().getContentResolver().getType(ContactContract.URI.buildUpon().appendPath("error").build());
        assertNull(type);
    }

    public void testBasicCRUD() throws Exception {
        ContentValues insertData = DataUtilities.createInsertContactValues();
        Uri uri = getContext().getContentResolver().insert(ContactContract.URI, insertData);
        assertNotNull(uri);

        long id = ContentUris.parseId(uri);

        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        CursorUtilities.validateCursor("Insert", cursor, insertData, id);

        // Update our only row with a new birth date using the "uri" provided on insert
        ContentValues updateData = DataUtilities.createUpdateBirthDate();
        int rows = getContext().getContentResolver().update(uri, updateData, null, null);
        assertEquals(1, rows);


        ContentValues mergedData = DataUtilities.mergeValues(insertData, updateData);
        cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        CursorUtilities.validateCursor("Update", cursor, mergedData, id);

        rows = getContext().getContentResolver().delete(uri, null, null);
        assertEquals(1, rows);

        cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
    }

    public void testBulkCRUD() throws Exception {
        ContentValues[] insertData = DataUtilities.createBulkInsertContactValues();

        int rows = getContext().getContentResolver().bulkInsert(ContactContract.URI, insertData);
        assertEquals(2, rows);

        Cursor cursor = getContext().getContentResolver().query(ContactContract.URI, null, null, null, null);
        CursorUtilities.validateCursor("Bulk Insert", cursor, insertData);

        // Update our only row with a new statehood date using the "uri" provided on insert
        ContentValues updateData = DataUtilities.createUpdateBirthDate();
        rows = getContext().getContentResolver().update(ContactContract.URI, updateData, null, null);
        assertEquals(2, rows);

        ContentValues[] mergedData = DataUtilities.mergeValues(insertData, updateData);
        cursor = getContext().getContentResolver().query(ContactContract.URI, null, null, null, null);
        CursorUtilities.validateCursor("Update", cursor, mergedData);

        rows = getContext().getContentResolver().delete(ContactContract.URI, null, null);
        assertEquals(2, rows);

        cursor = getContext().getContentResolver().query(ContactContract.URI, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
    }

    public void testErrors() throws Exception {
        // insert a valid row
        ContentValues insertData = DataUtilities.createInsertContactValues();
        Uri uri = getContext().getContentResolver().insert(ContactContract.URI, insertData);
        assertNotNull(uri);

        // Attempt to insert using returned uri.
        boolean hadException = false;
        try {
            uri = getContext().getContentResolver().insert(uri, insertData);
        }
        catch (UnsupportedOperationException e) {
            hadException = true;
        }
        assertTrue(hadException);

        Uri errorUri = ContactContract.URI.buildUpon().appendPath("error").build();
        hadException = false;
        try {
            uri = getContext().getContentResolver().insert(errorUri, insertData);
        }
        catch (UnsupportedOperationException e) {
            hadException = true;
        }
        assertTrue(hadException);

        hadException = false;
        try {
            Cursor cursor = getContext().getContentResolver().query(errorUri, null, null, null, null);
        }
        catch (UnsupportedOperationException e) {
            hadException = true;
        }
        assertTrue(hadException);

        hadException = false;
        int rows = 0;
        try {
            rows = getContext().getContentResolver().update(errorUri, insertData, null, null);
        }
        catch (UnsupportedOperationException e) {
            hadException = true;
        }
        assertTrue(hadException);

        hadException = false;
        try {
            rows = getContext().getContentResolver().delete(errorUri, null, null);
        }
        catch (UnsupportedOperationException e) {
            hadException = true;
        }
        assertTrue(hadException);
    }

}