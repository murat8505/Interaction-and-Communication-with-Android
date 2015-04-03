package com.example.joeroger.samplesearchshare.loader;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;

import com.example.joeroger.samplesearchshare.provider.ContactContract;

import java.lang.ref.WeakReference;

/**
 * City Loader callbacks. This class is part mandatory implementation of the LoaderCallbacks
 * for a loader and part abstraction to allow Activities/Fragments to avoid implementing this code
 * directly and reuse it directly.
 * <p/>
 * As you can see this is a lot of code and almost all of the code would have to be implemented
 * in the activity or fragment. The only savings you would have is a slight ability to avoid creating a
 * listener. However, since this should be a "static" inner class in an activity/fragment you would still
 * need a weak reference to the activity/fragment to receive data changes.
 */
public final class ContactLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ContactLoaderCB";

    private static final String ARG_URI = "uri";
    private static final String ARG_PROJECTION = "projection";
    private static final String ARG_QUERY_TEXT = "queryText";

    private final Context applicationContext;
    private final WeakReference<ContactLoadListener> listenerRef;

    public interface ContactLoadListener {
        public void onContactLoadComplete(Cursor cursor);
    }

    /**
     * Initialize a loader. Call from an Activity's onCreate or a Fragment's onActivityCreated method.
     */
    public static void initLoader(Context context, LoaderManager loaderManager, ContactLoadListener listener, String[] projection) {
        initLoader(context, loaderManager, listener, projection, null);
    }

    public static void initLoader(Context context, LoaderManager loaderManager, ContactLoadListener listener, String[] projection, String queryText) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, ContactContract.URI);
        args.putStringArray(ARG_PROJECTION, projection);
        args.putString(ARG_QUERY_TEXT, queryText);
        loaderManager.initLoader(LoaderIds.CONTACT_LOADER,
                args, new ContactLoaderCallbacks(context, listener));

    }

    /**
     * Additional method to restart loader. Used by query suggestions.
     * @param context
     * @param loaderManager
     * @param listener
     * @param projection
     * @param selection
     */
    public static void restartLoader(Context context, LoaderManager loaderManager, ContactLoadListener listener, String[] projection, String queryText) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, ContactContract.URI);
        args.putStringArray(ARG_PROJECTION, projection);
        args.putString(ARG_QUERY_TEXT, queryText);
        loaderManager.restartLoader(LoaderIds.CONTACT_LOADER,
                args, new ContactLoaderCallbacks(context, listener));
    }

    /**
     * Initialize a loader to query a specific city using the provided city id.
     */
    public static void initLoader(Context context, LoaderManager loaderManager, ContactLoadListener listener, String[] projection, long contactId) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, ContentUris.withAppendedId(ContactContract.URI, contactId));
        args.putStringArray(ARG_PROJECTION, projection);
        loaderManager.initLoader(LoaderIds.CONTACT_LOADER,
                args, new ContactLoaderCallbacks(context, listener));
    }

    /**
     * Private constructor to prevent direct instantiation
     *
     * @param context  The context to use for starting the loader
     * @param listener The listener to use to provide city cursor data back to the caller.
     */
    private ContactLoaderCallbacks(Context context, ContactLoadListener listener) {
        applicationContext = context.getApplicationContext();
        listenerRef = new WeakReference<>(listener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        Log.d(TAG, "onCreateLoader");
        Uri uri = bundle.getParcelable(ARG_URI);

        String selection = null;
        String selectionArgs[] = null;
        String queryText = bundle.getString(ARG_QUERY_TEXT);
        if (!TextUtils.isEmpty(queryText)) {
            selection = ContactContract.Columns.NAME + " like ?";
            selectionArgs = new String[] { "%" + queryText + "%"};
        }
        return new CursorLoader(
                applicationContext,
                uri,
                bundle.getStringArray(ARG_PROJECTION),
                selection,
                selectionArgs,
                ContactContract.Columns.NAME + " asc, " + ContactContract.Columns._ID + " asc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished result is " + (cursor != null ? "not null" : "null"));

        // Have a result. Note, it may be null, but we want to tell our listener if
        // we still have one with the value regardless.
        ContactLoadListener listener = listenerRef.get();
        if (listener != null) {
            Log.d(TAG, "onLoadFinished. Notifying listener");
            listener.onContactLoadComplete(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.d(TAG, "onLoadReset");
        onLoadFinished(cursorLoader, null);
    }
}
