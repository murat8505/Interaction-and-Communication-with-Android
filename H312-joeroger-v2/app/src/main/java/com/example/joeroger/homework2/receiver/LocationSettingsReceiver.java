package com.example.joeroger.homework2.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.example.joeroger.homework2.BuildConfig;
import com.google.android.gms.common.api.Status;

import java.lang.ref.WeakReference;


public class LocationSettingsReceiver extends BroadcastReceiver {
    private static final String ACTION_SETTINGS = BuildConfig.APPLICATION_ID + ".receiver.LocationReceiver.action.settings";

    private static final String EXTRA_SETTINGS = BuildConfig.APPLICATION_ID + ".receiver.LocationReceiver.extra.settings";

    private final Context applicationContext;
    private final WeakReference<LocationSettingsListener> listenerRef;

    public interface LocationSettingsListener {

        void onLocationSettingsResolutionNeeded(Status status);
    }

    public static void sendLocationSettings(Context context, Status status) {
        Intent intent = new Intent(ACTION_SETTINGS);
        intent.putExtra(EXTRA_SETTINGS, status);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    public LocationSettingsReceiver(Context context, LocationSettingsListener listener) {
        applicationContext = context.getApplicationContext();
        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(applicationContext);
        listenerRef = new WeakReference<>(listener);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SETTINGS);
        mgr.registerReceiver(this, filter);
    }

    public void unregister() {
        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(applicationContext);
        mgr.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LocationSettingsListener listener = listenerRef.get();
        if (listener == null) return;

        String action = intent.getAction();
        switch (action) {
            case ACTION_SETTINGS:
                Status status = intent.getParcelableExtra(EXTRA_SETTINGS);
                listener.onLocationSettingsResolutionNeeded(status);
                break;
        }
    }
}
