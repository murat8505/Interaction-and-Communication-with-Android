package com.example.bchan92.homework1.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;

public class NetworkUtils {

    /**
     * Retrieve the current connected network
     * @param context The context to use for retrieving the connected network
     * @return The current connected network (isConnected is true) or null if no network is active and connected
     */
    public static @Nullable
    NetworkInfo getConnectedNetwork(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnected()) {
            return activeNetwork;
        }

        return null;
    }

    public static boolean isConnected(Context context) {
        return getConnectedNetwork(context) != null;
    }

    public static boolean isNotConnected(Context context) {
        return getConnectedNetwork(context) == null;
    }
}
