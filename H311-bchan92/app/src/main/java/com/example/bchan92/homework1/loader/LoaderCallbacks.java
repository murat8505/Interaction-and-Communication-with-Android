package com.example.bchan92.homework1.loader;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

/**
 * Created by Brian on 4/6/15.
 */

public final class LoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
