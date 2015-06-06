package com.example.joeroger.samplemaps.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.lang.ref.WeakReference;

public class NetworkStatusBroadcastReceiver extends BroadcastReceiver {
    private static final String ACTION_LOAD_SUCCESS = "NetworkStatusReceiver.action.success";
    private static final String ACTION_LOAD_ERROR = "NetworkStatusReceiver.action.error";

    private final Context applicationContext;
    private final WeakReference<NetworkStatusListener> listenerRef;

    public interface NetworkStatusListener {
        public void onSuccess();

        public void onError();
    }

    public static void sendSuccessBroadcast(Context context) {
        Intent intent = new Intent(ACTION_LOAD_SUCCESS);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    public static void sendErrorBroadcast(Context context) {
        Intent intent = new Intent(ACTION_LOAD_ERROR);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    public NetworkStatusBroadcastReceiver(Context context, NetworkStatusListener listener) {
        applicationContext = context.getApplicationContext();
        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(applicationContext);
        listenerRef = new WeakReference<>(listener);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_LOAD_SUCCESS);
        filter.addAction(ACTION_LOAD_ERROR);
        mgr.registerReceiver(this, filter);
    }

    public void unregister() {
        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(applicationContext);
        mgr.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkStatusListener listener = listenerRef.get();
        if (listener == null) return;

        String action = intent.getAction();
        switch (action) {
            case ACTION_LOAD_SUCCESS:
                listener.onSuccess();
                break;
            case ACTION_LOAD_ERROR:
                listener.onError();
                break;
        }
    }
}
