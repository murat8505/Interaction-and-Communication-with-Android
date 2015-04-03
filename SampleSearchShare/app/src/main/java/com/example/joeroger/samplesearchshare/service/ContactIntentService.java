package com.example.joeroger.samplesearchshare.service;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.example.joeroger.samplesearchshare.provider.ContactContract;

public class ContactIntentService extends IntentService {

    private static final String ACTION_INSERT = ContactIntentService.class.getName() + ".INSERT";
    private static final String ACTION_UPDATE = ContactIntentService.class.getName() + ".UPDATE";
    private static final String ACTION_DELETE = ContactIntentService.class.getName() + ".DELETE";

    private static final String EXTRA_ID = ContactIntentService.class.getName() + ".id";
    private static final String EXTRA_VALUES = ContactIntentService.class.getName() + ".values";

    /**
     * Starts this service to perform action create. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startInsert(Context context, ContentValues values) {
        Intent intent = new Intent(context, ContactIntentService.class);
        intent.setAction(ACTION_INSERT);
        intent.putExtra(EXTRA_VALUES, values);
        context.startService(intent);
    }

    public static void startUpdate(Context context, long contactId, ContentValues values) {
        Intent intent = new Intent(context, ContactIntentService.class);
        intent.setAction(ACTION_UPDATE);
        intent.putExtra(EXTRA_ID, contactId);
        intent.putExtra(EXTRA_VALUES, values);
        context.startService(intent);
    }

    public static void startDelete(Context context, long contactId) {
        Intent intent = new Intent(context, ContactIntentService.class);
        intent.setAction(ACTION_DELETE);
        intent.putExtra(EXTRA_ID, contactId);
        context.startService(intent);
    }

    public ContactIntentService() {
        super("ContactIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            // Only run once per start to avoid db overhead since data is static to this class
            if (ACTION_INSERT.equals(action)) {
                ContentValues values = intent.getParcelableExtra(EXTRA_VALUES);
                handleActionCreate(values);
            }
            else if (ACTION_UPDATE.equals(action)) {
                Uri uri = getUriFromIntent(intent);
                ContentValues values = intent.getParcelableExtra(EXTRA_VALUES);
                handleActionUpdate(uri, values);
            }
            else if (ACTION_DELETE.equals(action)) {
                Uri uri = getUriFromIntent(intent);
                handleActionDelete(uri);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCreate(ContentValues values) {
        getContentResolver().insert(ContactContract.URI, values);
    }

    private void handleActionUpdate(Uri uri, ContentValues values) {
        getContentResolver().update(uri, values, null, null);
    }

    private void handleActionDelete(Uri uri) {
        getContentResolver().delete(uri, null, null);
    }

    private Uri getUriFromIntent(Intent intent) {
        long id = intent.getLongExtra(EXTRA_ID, ContactContract.NO_CONTACT_ID);
        Uri uri = ContactContract.URI;
        if (id != ContactContract.NO_CONTACT_ID) {
            uri = ContentUris.withAppendedId(uri, id);
        }
        return uri;
    }

}
