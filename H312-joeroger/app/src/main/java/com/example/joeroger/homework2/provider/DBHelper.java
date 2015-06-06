package com.example.joeroger.homework2.provider;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.example.joeroger.homework2.BuildConfig;


public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";

    private static final int DATABASE_VERSION = 2;
    /* package */ static final String DATABASE_NAME = "Weather.db";

    private static DBHelper dbHelper;

    public static synchronized DBHelper getInstance(Context context) {
        // If under test, just return a new helper using the provided context.
        if (BuildConfig.DEBUG && context.getClass().getName().contains("android.test")) {
            return new DBHelper(context);
        }

        if (dbHelper == null) {
            // Use application context because this is a singleton and do
            // not want it to reference a "specific" context"
            dbHelper = new DBHelper(context.getApplicationContext());
        }

        return dbHelper;
    }

    private DBHelper(Context context) {
        // Using application context since if in a singleton, the helper likely will
        // outlive the activity, asyncTask etc that starts it.

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
        setWriteAheadLoggingEnabled(true);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        Log.d(TAG, "onConfigure");
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

        Log.d(TAG, "onOpen");

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
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CityConditionsContract.CREATE_TABLE);
        db.execSQL(ForecastContract.CREATE_TABLE);
        db.execSQL(ForecastContract.CREATE_FORECAST_UNIQUE_INDEX);
        db.execSQL(ForecastContract.CREATE_FORECAST_UNIQUE_DT_INDEX);
        db.execSQL(DailyForecastContract.CREATE_TABLE);
        db.execSQL(DailyForecastContract.CREATE_DAILY_FORECAST_UNIQUE_INDEX);
        db.execSQL(DailyForecastContract.CREATE_DAILY_FORECAST_UNIQUE_DT_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO, do something to preserve cities
        db.execSQL("DROP INDEX IF EXISTS " + DailyForecastContract.DAILY_FORECAST_UNIQUE_DT_INDEX);
        db.execSQL("DROP INDEX IF EXISTS " + DailyForecastContract.DAILY_FORECAST_UNIQUE_INDEX);
        db.execSQL("DROP TABLE IF EXISTS " + DailyForecastContract.TABLE);
        db.execSQL("DROP INDEX IF EXISTS " + ForecastContract.FORECAST_UNIQUE_DT_INDEX);
        db.execSQL("DROP INDEX IF EXISTS " + ForecastContract.FORECAST_UNIQUE_INDEX);
        db.execSQL("DROP TABLE IF EXISTS " + ForecastContract.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CityConditionsContract.TABLE);
        onCreate(db);

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
