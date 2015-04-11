package com.example.samplenetworking.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;

import com.example.samplenetworking.network.CacheUtils;
import com.example.samplenetworking.network.NetworkUtils;
import com.example.samplenetworking.provider.Seattle911Contract;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * IntentService to fetch 911 data from the network and store in the database.
 * Also removes old 911 data that is no longer needed.
 */
public class NetworkIntentService extends IntentService {

    private static final String TAG = "NetworkIntentService";

    private static final String ACTION_LOAD = NetworkIntentService.class.getName() + ".action_load";
    private static final String EXTRA_BACKOFF = NetworkIntentService.class.getName() + ".backoff";

    private static final String WHERE_CLEARANCE_DATE_EXPIRED = Seattle911Contract.Columns.CLEARANCE_DATE + " < ?";
    private static long DEFAULT_BACKOFF = 500;

    private long backoff = DEFAULT_BACKOFF;

    public static void startService(Context context) {
        startService(context, DEFAULT_BACKOFF);
    }

    private static void startService(Context context, long backoff) {
        Intent intent = new Intent(context, NetworkIntentService.class);
        intent.setAction(ACTION_LOAD);
        intent.putExtra(EXTRA_BACKOFF, backoff);
        context.startService(intent);
    }

    public NetworkIntentService() {
        super("NetworkIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (ensureLatestSSL()) {

            Log.d(TAG, "starting");
            if (intent != null) {
                final String action = intent.getAction();
                backoff = intent.getLongExtra(EXTRA_BACKOFF, DEFAULT_BACKOFF);
                if (ACTION_LOAD.equals(action)) {
                    handleActionLoad();
                }
            }
            Log.d(TAG, "done");
        }
    }

    private boolean ensureLatestSSL() {

        try {
            // Ensure the latest SSL per
            // http://developer.android.com/training/articles/security-gms-provider.html
            ProviderInstaller.installIfNeeded(this);
            return true;
        }
        catch (GooglePlayServicesRepairableException e) {
            // Since this is a background service, show a notification
            GooglePlayServicesUtil.showErrorNotification(e.getConnectionStatusCode(), this);
            Log.d(TAG, "Repairable error updating SSL");
            return false;
        }
        catch (GooglePlayServicesNotAvailableException e) {
            // Since this is a background service, show a notification
            GooglePlayServicesUtil.showErrorNotification(e.errorCode, this);
            Log.d(TAG, "Missing play servers updating SSL");
            return false;
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionLoad() {
        CacheUtils.initializeCache(this);

        // No network, we should get out
        if (NetworkUtils.isNotConnected(this)) {
            delayAndRetry();
            return;
        }

        try {
            URL url = buildUrl();
            ContentValues[] values = performGet(url);
            if (values != null) {
                getContentResolver().bulkInsert(Seattle911Contract.URI, values);

                // Purge anything older than 48 hours when we do an update.
                Calendar expired = Calendar.getInstance();
                expired.add(Calendar.DATE, -2);
                String[] selectionArgs = new String[]{Long.toString(expired.getTimeInMillis())};
                int rows = getContentResolver().delete(Seattle911Contract.URI, WHERE_CLEARANCE_DATE_EXPIRED, selectionArgs);
                Log.d(TAG, "Purged rows: " + rows);
            }
        }
        catch (Exception e) {
            Log.w(TAG, "Unexpected error", e);
        }

        CacheUtils.logCache();
    }

    private URL buildUrl() throws MalformedURLException {
        // Want to fetch from "midnight" local time yesterday.
        TimeZone timezone = TimeZone.getTimeZone("America/Los_Angeles");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        formatter.setTimeZone(timezone);

        // Midnight yesterday.
        Calendar calendar = Calendar.getInstance(timezone);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, -1);

        // Using URI builder to ensure url and parameter are properly encoded
        Uri uri = new Uri.Builder()
                .scheme("https")
                .authority("data.seattle.gov")
                .appendPath("resource")
                .appendPath("3k2p-39jp.json")
                .appendQueryParameter("$where", "event_clearance_date>'"
                        + formatter.format(calendar.getTime()) + "'").build();


        Log.d(TAG, uri.toString());
        return new URL(uri.toString());
    }

    private ContentValues[] performGet(URL url) throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setRequestMethod("GET");

            // Force network request, ignoring cache
            // urlConnection.addRequestProperty("Cache-Control", "no-cache");

            // Check to see if cache is stale. Forces a server validation of the cache
            // and reuse cache if hasn't changed.
            // urlConnection.addRequestProperty("Cache-Control", "max-age=0");

            // Reuse data only if its cached and valid. Can be useful for when offline. However,
            // if the cache data has expired, the cache will respond with status code 504 per
            // the RFC specification. (Note: for this assignment is a way to see the backoff logic)
            // urlConnection.addRequestProperty("Cache-Control", "only-if-cached");

            // Allow stale cache data. Useful if offline to allow re-using data from the cache
            // even if it is stale. Better version of only-if-cached as it will return the stale
            // data up to the time limit.
            // urlConnection.addRequestProperty("Cache-Control", "max-stale=600000");

            // Using 15 sec timeouts.
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);
            urlConnection.connect();

            int status = urlConnection.getResponseCode();

            // For the Seattle 911 api, this response, means retry...
            if (status == HttpURLConnection.HTTP_ACCEPTED) {
                delayAndRetry();
            }
            // For Ok, process result
            if (status == HttpURLConnection.HTTP_OK) {
                return buildContentValues(urlConnection.getInputStream());
            }

            Log.w(TAG, "status: " + status);

            // Only logging contents. Depending on the API, there may be other info that is
            // useful to the application, developer, or possibly the user. For example if
            // you change the url and misspell "event" in the column name it indicates that
            // the column was not found.
            logErrorStream(urlConnection.getErrorStream());
            delayAndRetry();
            return null;
        }
        finally {
            // Return connection back to the pool or to be closed.
            urlConnection.disconnect();
        }
    }


    private void delayAndRetry() {

        double RANDOM_FACTOR = 0.5d; // 50% below + 50% above
        double BACKOFF_INTERVAL = 1.5d; // 50% backoff rate
        double MAX_BACKOFF = 15000; // Max backoff

        double minInterval = backoff - backoff * RANDOM_FACTOR;
        double maxInterval = backoff + backoff * RANDOM_FACTOR;
        double random = Math.random();
        long delay = Math.round(minInterval + random * (maxInterval - minInterval + 1));

        try {
            Log.d(TAG, "Sleeping: " + delay);
            Thread.sleep(delay);
        }
        catch (InterruptedException e) {
            // do nothing;
        }

        // Compute next backoff, use random so not all apps hit server at same time
        long newBackoff = Math.round(backoff * BACKOFF_INTERVAL);
        if (newBackoff < MAX_BACKOFF) {
            Log.d(TAG, "New backoff: " + newBackoff);
            startService(this, newBackoff);
        }
        else {
            Log.d(TAG, "Hit max backoff, stopping");
        }
    }


    private ContentValues[] buildContentValues(InputStream stream) throws IOException {

        // Ideally determine encoding from the HTTP response headers vs hardcoding to UTF-8.
        // However, UTF-8 is also most common format.
        JsonReader jsonReader = new JsonReader(new InputStreamReader(stream, "UTF-8"));

        // Note: AS will indicate that you can use try with resources because Java 7 is
        // target. However, try with resources is not compatible prior to SDK 19.
        try {
            return processArray(jsonReader);
        }
        finally {
            jsonReader.close();
        }
    }

    private ContentValues[] processArray(JsonReader jsonReader) throws IOException {
        ArrayList<ContentValues> list = new ArrayList<>();

        // Dates are given in Seattle time. So need to convert back to UTC for easy storage
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            list.add(processObject(jsonReader, formatter));
        }
        jsonReader.endArray();

        ContentValues[] values = new ContentValues[list.size()];
        return list.toArray(values);
    }

    private ContentValues processObject(JsonReader jsonReader, SimpleDateFormat formatter) throws IOException {

        ContentValues values = new ContentValues();

        jsonReader.beginObject();

        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case Seattle911Contract.Json.OFFENSE_ID:
                    values.put(Seattle911Contract.Columns.OFFENSE_ID, jsonReader.nextLong());
                    break;
                case Seattle911Contract.Json.DESCRIPTION:
                    values.put(Seattle911Contract.Columns.DESCRIPTION, jsonReader.nextString());
                    break;
                case Seattle911Contract.Json.CATEGORY:
                    values.put(Seattle911Contract.Columns.CATEGORY, jsonReader.nextString());
                    break;
                case Seattle911Contract.Json.SUB_CATEGORY:
                    values.put(Seattle911Contract.Columns.SUB_CATEGORY, jsonReader.nextString());
                    break;
                case Seattle911Contract.Json.LOCATION:
                    values.put(Seattle911Contract.Columns.LOCATION, jsonReader.nextString());
                    break;
                case Seattle911Contract.Json.LONGITUDE:
                    values.put(Seattle911Contract.Columns.LONGITUDE, jsonReader.nextDouble());
                    break;
                case Seattle911Contract.Json.LATITUDE:
                    values.put(Seattle911Contract.Columns.LATITUDE, jsonReader.nextDouble());
                    break;
                case Seattle911Contract.Json.CLEARANCE_DATE:
                    try {
                        Date date = formatter.parse(jsonReader.nextString());
                        values.put(Seattle911Contract.Columns.CLEARANCE_DATE, date.getTime());
                    }
                    catch (ParseException e) {
                        Log.w(TAG, "Failed to parse date", e);
                    }
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }

        jsonReader.endObject();

        return values;
    }

    private void logErrorStream(@Nullable InputStream errorStream) throws IOException {
        if (errorStream == null) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"));
        try {
            String result;
            while ((result = reader.readLine()) != null) {
                builder.append(result);
            }
        }
        finally {
            reader.close();
        }

        Log.w(TAG, builder.toString());
    }
}
