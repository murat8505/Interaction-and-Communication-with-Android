package com.example.joeroger.samplemaps.provider;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    /* package */ static final String DATABASE_NAME = "Seattle911.db";

    private static DBHelper dbHelper;

    public static synchronized DBHelper getInstance(Context context) {
        if (dbHelper == null) {
            // Using application context since if in a singleton, the helper likely will
            // outlive the activity, asyncTask etc that starts it.
            dbHelper = new DBHelper(context.getApplicationContext());
        }

        return dbHelper;
    }

    private DBHelper(Context context) {
        // the "null" indicates we want to use the default cursor factory
        super(context, DATABASE_NAME, null, DATABASE_VERSION);


        // Write ahead logging allows multiple threads access the database
        // at same time which improves performance. Essentially a writer will no
        // longer block a reader accessing the database at the same time.
        // This method configures it for the active or next instance.
        // It can also be used to toggle it off.
        //
        // Note: if you use transactions, you need to run the transactions in
        // non-exclusive mode see
        // http://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html#enableWriteAheadLogging()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setWriteAheadLoggingEnabled(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        // By default foreign key constraints are NOT enforced. This
        // was a surprise when I first encountered. Therefore you should turn
        // them on if you actually want them to help you debug schema problems.
        //
        // Currently not using, but why not
        //
        // Note: if you do add a foreign key, you should also create an index for
        // each one as it should help with performance.
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        // If you are supporting OS versions before JellyBean, the onConfigure and write
        // ahead logging methods do not exist.  This is how you activate
        // for older releases.  You can only do this on a writable database.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && !db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
            db.enableWriteAheadLogging();
        }

        // Force a checkpoint on database open. Useful if writes do not occur often.
        // This code only applies if write ahead logging is enabled
        if (!db.isReadOnly()) {
            // This pragma reduces number of pages to auto checkpoint from 1000 to 100.
            // The default page size is 4k. Tune this to determine how frequent you
            // want the checkpoints to process. 100 ~= 400k of write ahead data not
            // checkpointed back into the database.
            Cursor c = db.rawQuery("PRAGMA wal_autocheckpoint=100", null);
            if (c != null) {
                c.close();
            }

            // This pragma forces a checkpoint first time a writable database is opened.
            // Basically sync the database with the checkpoint when the database is first opened
            // for writing. Essentially designed to improve "read" performance if data is "stable"
            // and doesn't change often.
            c = db.rawQuery("PRAGMA wal_checkpoint", null);
            if (c != null) {
                c.close();
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // This is where you create the tables for the database. Should create in
        // dependency order. Since city has a FK to the state table, create state first.
        db.execSQL(Seattle911Contract.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // This is test code that completely rebuilds the database if the version changes.
        // If this is a temporary "cache", then its ok to completely delete and rebuild
        // as versions change. Although if it is a "cache" you may need to "delete" stale
        // data periodically between releases.
        cleanupDatabase(db);
        onCreate(db);
    }

    private static void cleanupDatabase(SQLiteDatabase db) {
        // Using if exists to prevent errors if the index or table does not exist
        db.execSQL("DROP TABLE IF EXISTS " + Seattle911Contract.TABLE);
    }
}
