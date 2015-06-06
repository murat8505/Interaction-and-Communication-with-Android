package com.example.joeroger.homework2.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.StringRes;
import android.support.v4.content.LocalBroadcastManager;

import com.example.joeroger.homework2.BuildConfig;

import java.lang.ref.WeakReference;

/**
 * Helper receiver to indicate status of adding a city.
 */
public class FavoriteCityReceiver extends BroadcastReceiver {
    private static final String ACTION_CITY_STATUS = BuildConfig.APPLICATION_ID + ".receiver.action.cityStatus";

    private static final String EXTRA_MSG_RES_ID = BuildConfig.APPLICATION_ID + ".receiver.extra.msgResId";
    private static final String EXTRA_REMOVED = BuildConfig.APPLICATION_ID + ".receiver.extra.removed";

    private final Context applicationContext;
    private final WeakReference<UpdateCityStatusListener> listenerRef;

    public interface UpdateCityStatusListener {

        void onCityStatus(int statusMsgResId, boolean isRemoved);
    }

    public static void sendAddCityStatus(Context context, @StringRes int msgResId) {
        sendCityStatus(context, msgResId, false);
    }

    public static void sendCityStatus(Context context, @StringRes int msgResId, boolean isRemoved) {
        Intent intent = new Intent(ACTION_CITY_STATUS);
        intent.putExtra(EXTRA_MSG_RES_ID, msgResId);
        intent.putExtra(EXTRA_REMOVED, isRemoved);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    public FavoriteCityReceiver(Context context, UpdateCityStatusListener listener) {
        applicationContext = context.getApplicationContext();
        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(applicationContext);
        listenerRef = new WeakReference<>(listener);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CITY_STATUS);
        mgr.registerReceiver(this, filter);
    }

    public void unregister() {
        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(applicationContext);
        mgr.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        UpdateCityStatusListener listener = listenerRef.get();
        if (listener == null) return;

        String action = intent.getAction();
        switch (action) {
            case ACTION_CITY_STATUS:
                final int statusMsgResId = intent.getIntExtra(EXTRA_MSG_RES_ID, 0);
                final boolean isRemoved = intent.getBooleanExtra(EXTRA_REMOVED, false);
                if (statusMsgResId != 0) {
                    listener.onCityStatus(statusMsgResId, isRemoved);
                }
                break;
        }
    }
}
