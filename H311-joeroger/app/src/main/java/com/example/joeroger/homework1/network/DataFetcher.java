package com.example.joeroger.homework1.network;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.joeroger.BuildConfig;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public final class DataFetcher {
    private static final String TAG = "DataFetcher";

    public interface DataConverter {
        @Nullable
        public ContentValues[] convertData(@NonNull InputStream inputStream);
    }


    public static boolean ensureLatestSSL(Context context) {

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

    public static ContentValues[] performGet(URL url, DataConverter converter) {

        if (url == null || converter == null) {
            return null;
        }

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            // Using 15 sec timeouts.
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);
            urlConnection.connect();

            int status = urlConnection.getResponseCode();

            // For Ok, process result
            if (status == HttpURLConnection.HTTP_OK) {
                return converter.convertData(urlConnection.getInputStream());
            }

            Log.w(TAG, "status: " + status);

            // Only logging contents. Depending on the API, there may be other info that is
            // useful to the application, developer, or possibly the user. For example if
            // you change the url and misspell "event" in the column name it indicates that
            // the column was not found.
            logErrorStream(urlConnection.getErrorStream());
            return null;
        }
        catch (IOException ex) {
            Log.e(TAG, "Failed to load data: " + ex.getMessage());
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "exception", ex);
            }
            return null;
        }
        finally {
            // Return connection back to the pool or to be closed.
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
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
