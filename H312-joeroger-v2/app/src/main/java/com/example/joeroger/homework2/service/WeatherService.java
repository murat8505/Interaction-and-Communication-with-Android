package com.example.joeroger.homework2.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.example.joeroger.homework2.BuildConfig;

public class WeatherService extends IntentService {
    private static final String TAG = "WeatherService";

    private static final String ACTION_FETCH_WEATHER = BuildConfig.APPLICATION_ID + ".service.action.FETCH_WEATHER";


    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFetch(Context context) {
        context.startService(buildIntent(context));
    }

    public static Intent buildIntent(Context context) {
        Intent intent = new Intent(context, WeatherService.class);
        intent.setAction(ACTION_FETCH_WEATHER);
        return intent;
    }

    public WeatherService() {
        super("WeatherService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_FETCH_WEATHER:
                    try {
                        handleActionFetchWeather();
                    }
                    finally {
                        WakefulBroadcastReceiver.completeWakefulIntent(intent);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void handleActionFetchWeather() {
        ServiceHelper helper = new ServiceHelper(this);
        helper.fetchData();
    }
}
