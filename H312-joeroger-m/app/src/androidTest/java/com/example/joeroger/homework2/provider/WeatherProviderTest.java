package com.example.joeroger.homework2.provider;

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
public class WeatherProviderTest extends ProviderTestCase2<WeatherProvider> {

    public WeatherProviderTest() {
        super(WeatherProvider.class, BaseContract.AUTHORITY);
    }

    @Before
    public void compatibleSetUp() throws Exception {
        // inject context first as setup depends on it. Since this is
        // JUnit4, get the context from the instrumentation registry
        setContext(InstrumentationRegistry.getTargetContext());
        setUp();
        cleanupDB();
    }

    private void cleanupDB() {
        // Use the provider's context so everything is using the same context
        DBHelper helper = DBHelper.getInstance(getProvider().getContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(CityConditionsContract.TABLE, null, null);
        db.close();
    }

    @After
    public void compatibleTearDown() throws Exception {
        tearDown();
    }

    @Test
    public void testGetType() throws Exception {
        // Verify the right type is returned using the standard URI
        String type = getMockContentResolver().getType(CityConditionsContract.URI);
        assertEquals(CityConditionsContract.CONTENT_TYPE, type);

        // Verify the right type is returned using a URI with an _ID attached.
        type = getMockContentResolver().getType(ContentUris.withAppendedId(CityConditionsContract.URI, 1));
        assertEquals(CityConditionsContract.CONTENT_ITEM_TYPE, type);

        // Verify a bad uri
        type = getMockContentResolver().getType(CityConditionsContract.URI.buildUpon().appendPath("error").build());
        assertNull(type);

        // Verify the right type is returned using the standard URI
        type = getMockContentResolver().getType(ForecastContract.URI);
        assertEquals(ForecastContract.CONTENT_TYPE, type);

        // Verify the right type is returned using a URI with an _ID attached.
        type = getMockContentResolver().getType(ContentUris.withAppendedId(ForecastContract.URI, 1));
        assertEquals(ForecastContract.CONTENT_ITEM_TYPE, type);

        // Verify a bad uri
        type = getMockContentResolver().getType(ForecastContract.URI.buildUpon().appendPath("error").build());
        assertNull(type);

        // Verify the right type is returned using the standard URI
        type = getMockContentResolver().getType(DailyForecastContract.URI);
        assertEquals(DailyForecastContract.CONTENT_TYPE, type);

        // Verify the right type is returned using a URI with an _ID attached.
        type = getMockContentResolver().getType(ContentUris.withAppendedId(DailyForecastContract.URI, 1));
        assertEquals(DailyForecastContract.CONTENT_ITEM_TYPE, type);

        // Verify a bad uri
        type = getMockContentResolver().getType(DailyForecastContract.URI.buildUpon().appendPath("error").build());
        assertNull(type);
    }

    @Test
    public void testCityConditionsCrud() {

        long lastTime = System.currentTimeMillis();
        ContentValues insertData = DataUtilities.createCityConditionValues();
        Uri uri = getMockContentResolver().insert(CityConditionsContract.URI, insertData);
        assertNotNull(uri);

        long id = ContentUris.parseId(uri);

        Cursor cursor = getMockContentResolver().query(uri, null, null, null, null);
        CursorUtilities.validateCursor("Insert", cursor, insertData, id, lastTime);
        cursor.close();

        lastTime = System.currentTimeMillis();
        // Update our only row with a new birth date using the "uri" provided on insert
        ContentValues updateData = DataUtilities.createCityConditionReplaceValues();
        Uri replaceUri = getMockContentResolver().insert(CityConditionsContract.URI, updateData);
        assertFalse(uri.equals(replaceUri));

        id = ContentUris.parseId(replaceUri);
        cursor = getMockContentResolver().query(replaceUri, null, null, null, null);
        CursorUtilities.validateCursor("Replace", cursor, updateData, id, lastTime);
        cursor.close();

        int rows = getMockContentResolver().delete(uri, null, null);
        assertEquals(0, rows);

        rows = getMockContentResolver().delete(replaceUri, null, null);
        assertEquals(1, rows);

        cursor = getMockContentResolver().query(uri, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }
}