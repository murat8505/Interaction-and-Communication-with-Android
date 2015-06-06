package com.example.joeroger.samplemaps.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("JUnit4AnnotatedMethodInJUnit3TestCase")
@RunWith(AndroidJUnit4.class)
public class Seattle911ProviderTest extends ProviderTestCase2<Seattle911Provider> {

    public Seattle911ProviderTest() {
        super(Seattle911Provider.class, Seattle911Contract.AUTHORITY);
    }

    @Before
    public void compatibleSetUp() throws Exception {
        // inject context first as setup depends on it. Since this is
        // JUnit4, get the context from the instrumentation registry
        setContext(InstrumentationRegistry.getTargetContext());
        setUp();
        cleanupDB();
    }

    @After
    public void compatibleTearDown() throws Exception {
        tearDown();
    }

    private void cleanupDB() {
        // Use the provider's context so everything is using the same context
        DBHelper helper = DBHelper.getInstance(getProvider().getContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(Seattle911Contract.TABLE, null, null);
        db.close();
    }

    @Test
    public void testGetType() throws Exception {
        // Verify the right type is returned using the standard URI
        String type = getMockContentResolver().getType(Seattle911Contract.URI);
        assertEquals(Seattle911Contract.CONTENT_TYPE, type);

        // Verify the right type is returned using a URI with an _ID attached.
        type = getMockContentResolver().getType(ContentUris.withAppendedId(Seattle911Contract.URI, 1));
        assertEquals(Seattle911Contract.CONTENT_ITEM_TYPE, type);

        // Verify a bad uri
        type = getMockContentResolver().getType(Seattle911Contract.URI.buildUpon().appendPath("error").build());
        assertNull(type);
    }

    @Test
    public void testBasicCRUD() throws Exception {
        ContentValues insertData = DataUtilities.createInsertValues();
        Uri uri = getMockContentResolver().insert(Seattle911Contract.URI, insertData);
        assertNotNull(uri);

        long id = ContentUris.parseId(uri);

        Cursor cursor = getMockContentResolver().query(uri, null, null, null, null);
        CursorUtilities.validateCursor("Insert", cursor, insertData, id);
        cursor.close();

        // Update our only row with a new birth date using the "uri" provided on insert
        ContentValues updateData = DataUtilities.createUpdateValues();
        int rows = getMockContentResolver().update(uri, updateData, null, null);
        assertEquals(1, rows);


        ContentValues mergedData = DataUtilities.mergeValues(insertData, updateData);
        cursor = getMockContentResolver().query(uri, null, null, null, null);
        CursorUtilities.validateCursor("Update", cursor, mergedData, id);
        cursor.close();

        rows = getMockContentResolver().delete(uri, null, null);
        assertEquals(1, rows);

        cursor = getMockContentResolver().query(uri, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    @Test
    public void testErrors() throws Exception {
        // insert a valid row
        ContentValues insertData = DataUtilities.createInsertValues();
        Uri uri = getMockContentResolver().insert(Seattle911Contract.URI, insertData);
        assertNotNull(uri);

        // Attempt to insert using returned uri.
        boolean hadException = false;
        try {
            uri = getMockContentResolver().insert(uri, insertData);
        }
        catch (UnsupportedOperationException e) {
            hadException = true;
        }
        assertTrue(hadException);

        Uri errorUri = Seattle911Contract.URI.buildUpon().appendPath("error").build();
        hadException = false;
        try {
            uri = getMockContentResolver().insert(errorUri, insertData);
        }
        catch (UnsupportedOperationException e) {
            hadException = true;
        }
        assertTrue(hadException);

        hadException = false;
        try {
            Cursor cursor = getMockContentResolver().query(errorUri, null, null, null, null);
        }
        catch (UnsupportedOperationException e) {
            hadException = true;
        }
        assertTrue(hadException);

        hadException = false;
        int rows = 0;
        try {
            rows = getMockContentResolver().update(errorUri, insertData, null, null);
        }
        catch (UnsupportedOperationException e) {
            hadException = true;
        }
        assertTrue(hadException);

        hadException = false;
        try {
            rows = getMockContentResolver().delete(errorUri, null, null);
        }
        catch (UnsupportedOperationException e) {
            hadException = true;
        }
        assertTrue(hadException);
    }
}