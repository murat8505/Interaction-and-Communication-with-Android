package com.example.joeroger.samplebackgrounding.service;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;

import com.example.joeroger.samplebackgrounding.network.CacheUtils;
import com.example.joeroger.samplebackgrounding.network.ConnectivityUtils;
import com.example.joeroger.samplebackgrounding.provider.Seattle911Contract;
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

class FetchDataHelper {

    private static final String TAG = "FetchDataHelper";
    private static final String WHERE_CLEARANCE_DATE_EXPIRED = Seattle911Contract.Columns.CLEARANCE_DATE + " < ?";
    private static final long TWO_HOURS = 2 * 60 * 60 * 1000;
    private static final long TEN_MINUTES = 10 * 60 * 1000;
    private static final long DELAY_TIME = TEN_MINUTES;


    public interface DelayAndRetryHandler {
        public void delayAndRetry();
        public void delayAndRetryError();
        public boolean isLoadCancelled();
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    public static boolean loadData(Context context, DelayAndRetryHandler handler) {
        if (!ensureLatestSSL(context)) {
            return false;
        }

        CacheUtils.initializeCache(context);

        // No network, we should get out
        if (ConnectivityUtils.isNotConnected(context)) {
            handler.delayAndRetryError();
            return false;
        }

        try {
            Date lastFetch = determineLastFetch(context);

            // One way to save network cost is to know your last fetch time and expected data refresh
            // rate. Lets say Seattle 911 data is updated every two hours.  If its been two hours since the
            // last closed incident, its likely time to fetch new data.  Otherwise, we can just quit and
            // not waste any attempt to fetch data.
            if (lastFetch.getTime() > System.currentTimeMillis() - DELAY_TIME) {
                Log.d(TAG, "Skipping fetch");
                return true;
            }

            URL url = buildUrl(lastFetch);
            if (handler.isLoadCancelled()) {
                return false;
            }
            ContentValues[] values = performGet(url, handler);
            if (values != null) {
                if (handler.isLoadCancelled()) {
                    return false;
                }
                int rows = context.getContentResolver().bulkInsert(Seattle911Contract.URI, values);
                Log.d(TAG, "Inserted rows: " + rows);

                if (handler.isLoadCancelled()) {
                    return false;
                }
                // Purge anything older than 48 hours when we do an update. Since old data is not
                // useful to the app as currently envisioned, no need to burn storage holding it.
                Calendar expired = Calendar.getInstance();
                expired.add(Calendar.DATE, -2);
                String[] selectionArgs = new String[]{Long.toString(expired.getTimeInMillis())};
                rows = context.getContentResolver().delete(Seattle911Contract.URI, WHERE_CLEARANCE_DATE_EXPIRED, selectionArgs);
                Log.d(TAG, "Purged rows: " + rows);
            }
        }
        catch (Exception e) {
            Log.w(TAG, "Unexpected error", e);
            return false;
        }

        CacheUtils.logCache();
        return true;
    }

    private static boolean ensureLatestSSL(Context context) {

        try {
            // Ensure the latest SSL per
            // http://developer.android.com/training/articles/security-gms-provider.html
            ProviderInstaller.installIfNeeded(context);
            return true;
        }
        catch (GooglePlayServicesRepairableException e) {
            // Since this is a background service, show a notification
            GooglePlayServicesUtil.showErrorNotification(e.getConnectionStatusCode(), context);
            Log.d(TAG, "Repairable error updating SSL");
            return false;
        }
        catch (GooglePlayServicesNotAvailableException e) {
            // Since this is a background service, show a notification
            GooglePlayServicesUtil.showErrorNotification(e.errorCode, context);
            Log.d(TAG, "Missing play servers updating SSL");
            return false;
        }
    }

    private static URL buildUrl(Date lastFetch) throws MalformedURLException {
        // Want to fetch from "midnight" local time yesterday.
        TimeZone timezone = TimeZone.getTimeZone("America/Los_Angeles");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        formatter.setTimeZone(timezone);

        // Sync back top of hour of last fetch to allow a little overlap. I'm primarily doing this
        // because it isn't clear if the clearance dates are always moving forward. However, the other
        // advantage is the server possibly has queued to the top of an hour based on other queries vs
        // an ad-hoc time period, meaning the query performance would be faster if hit the server cache
        // vs a random timestamp.
        Calendar calendar = Calendar.getInstance(timezone);
        calendar.setTime(lastFetch);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

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

    private static Date determineLastFetch(Context context) {
        String[] projection = new String[]{"max(" + Seattle911Contract.Columns.CLEARANCE_DATE + ")"};
        Cursor cursor = context.getContentResolver().query(Seattle911Contract.URI, projection, null, null, null);
        Date lastFetch = new Date(0);
        try {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                lastFetch = new Date(cursor.getLong(0));
            }
        }
        finally {
            cursor.close();
        }

        // Compute 24 hours ago
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, -1);

        return lastFetch.after(calendar.getTime()) ? lastFetch : calendar.getTime();
    }

    @Nullable
    private static ContentValues[] performGet(URL url, DelayAndRetryHandler handler) throws IOException {

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
                handler.delayAndRetry();
            }
            // For Ok, process result
            if (status == HttpURLConnection.HTTP_OK) {
                if (handler.isLoadCancelled()) {
                    urlConnection.getInputStream().close();
                    return null;
                }
                return buildContentValues(urlConnection.getInputStream());
            }

            Log.w(TAG, "status: " + status);

            // Only logging contents. Depending on the API, there may be other info that is
            // useful to the application, developer, or possibly the user. For example if
            // you change the url and misspell "event" in the column name it indicates that
            // the column was not found.
            logErrorStream(urlConnection.getErrorStream());
            handler.delayAndRetryError();
            return null;
        }
        finally {
            // Return connection back to the pool or to be closed.
            urlConnection.disconnect();
        }
    }

    @NonNull
    private static ContentValues[] buildContentValues(InputStream stream) throws IOException {

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

    @NonNull
    private static ContentValues[] processArray(JsonReader jsonReader) throws IOException {
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

    private static ContentValues processObject(JsonReader jsonReader, SimpleDateFormat formatter) throws IOException {

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

    private static void logErrorStream(@Nullable InputStream errorStream) throws IOException {
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
