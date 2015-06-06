package com.example.joeroger.samplelocation.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;


public class ConnectivityUtils {

    /**
     * Retrieve the current connected network
     * @param context The context to use for retrieving the connected network
     * @return The current connected network (isConnected is true) or null if no network is active and connected
     */
    public static @Nullable NetworkInfo getConnectedNetwork(Context context) {
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

    public static boolean isConnectedWifi(Context context) {
        NetworkInfo activeNetwork = getConnectedNetwork(context);
        return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isConnectionMetered(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivity.isActiveNetworkMetered();
    }

    public static boolean isNotConnected(Context context) {
        return getConnectedNetwork(context) == null;
    }
}
