package com.example.joeroger.samplesearchshare.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.io.File;
import java.util.HashSet;

/**
 * Basic test class to test the database.
 */
public class DBHelperTest extends AndroidTestCase {


    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        getContext().deleteDatabase(DBHelper.DATABASE_NAME);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteTheDatabase();
    }

    public void testBuildDb() throws Throwable {

        File file = getContext().getDatabasePath(DBHelper.DATABASE_NAME);
        assertFalse("Database already exists", file.exists());

        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(ContactContract.TABLE);

        DBHelper helper = DBHelper.getInstance(getContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain both tables
        assertTrue("Error: Your database was created without correct tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + ContactContract.TABLE + ")", null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> stateColumnHashSet = new HashSet<>();
        stateColumnHashSet.add(ContactContract.Columns._ID);
        stateColumnHashSet.add(ContactContract.Columns.NAME);
        stateColumnHashSet.add(ContactContract.Columns.ADDRESS);
        stateColumnHashSet.add(ContactContract.Columns.BIRTH_DATE);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            stateColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        c.close();

        // if this fails, it means that your database doesn't contain all of the required contact
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required contact columns",
                stateColumnHashSet.isEmpty());

        db.close();
        helper.close();
    }

    public void testCRUDContactTable() throws Throwable {
        // CRUD = Create, Read, Update, Delete

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        DBHelper dbHelper = DBHelper.getInstance(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        ContentValues insertValues = DataUtilities.createInsertContactValues();

        // Third Step: "Create" Insert ContentValues into database and get a row ID back
        long rowId;
        rowId = db.insert(ContactContract.TABLE, null, insertValues);

        // Fourth Step: "Read" Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor insertCursor = db.query(
                ContactContract.TABLE,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Fifth Step: Validate the Query. Adding id values for validation purposes
        CursorUtilities.validateCursor("Contact Cursor", insertCursor, insertValues, rowId);
        insertCursor.close();

        // Sixth Step: Create content values for "update"
        ContentValues updateValues = DataUtilities.createUpdateBirthDate();


        // Seventh Step: Update table
        String selection = ContactContract.Columns._ID + " = ?";
        String[] selectionArgs = new String[]{Long.toString(rowId)};

        int rows = db.update(ContactContract.TABLE,
                updateValues,
                selection,
                selectionArgs);

        assertEquals("More than one row", rows, 1);

        // Eighth Step: "Read" Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor updateCursor = db.query(
                ContactContract.TABLE,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Ninth Step: Validate the Query. Adding id values for validation purposes
        ContentValues mergeValues = DataUtilities.mergeValues(insertValues, updateValues);
        CursorUtilities.validateCursor("Contact Cursor", updateCursor, mergeValues, rowId);
        updateCursor.close();

        // Tenth Step: Delete data
        rows = db.delete(ContactContract.TABLE, selection, selectionArgs);
        assertEquals("More than one row", rows, 1);

        // Eleventh Step: "Read" Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor deleteCursor = db.query(
                ContactContract.TABLE,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // verify cursor is empty
        assertFalse("NonEmpty cursor returned. ", deleteCursor.moveToFirst());
        deleteCursor.close();

        // Last Step: cleanup
        db.close();
        dbHelper.close();
    }
}
