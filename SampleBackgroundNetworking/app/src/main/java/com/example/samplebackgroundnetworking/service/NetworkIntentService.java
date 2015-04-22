package com.example.samplebackgroundnetworking.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.example.samplebackgroundnetworking.network.AlarmUtils;


/**
 * IntentService to fetch 911 data from the network and store in the database.
 * Also removes old 911 data that is no longer needed.
 */
public class NetworkIntentService extends IntentService {

    private static final String TAG = "NetworkIntentService";

    private static final String ACTION_LOAD = NetworkIntentService.class.getName() + ".action.load";
    private static final String ACTION_LOAD_WAKEFUL = NetworkIntentService.class.getName() + ".action.loadWakeful";
    private static final String EXTRA_BACKOFF = NetworkIntentService.class.getName() + ".extra.backoff";

    private static final long DEFAULT_BACKOFF = 500;


    public static void startService(Context context) {
        startService(context, DEFAULT_BACKOFF);
    }

    private static void startService(Context context, long backoff) {
        Intent intent = new Intent(context, NetworkIntentService.class);
        intent.setAction(ACTION_LOAD);
        intent.putExtra(EXTRA_BACKOFF, backoff);
        context.startService(intent);
    }


    public static Intent buildWakefulIntent(Context context, long backoff) {
        Intent intent = new Intent(context, NetworkIntentService.class);
        intent.setAction(ACTION_LOAD_WAKEFUL);
        intent.putExtra(EXTRA_BACKOFF, backoff);
        return intent;
    }

    public NetworkIntentService() {
        super("NetworkIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "starting");
        if (intent != null) {
            final String action = intent.getAction();
            long backoff = intent.getLongExtra(EXTRA_BACKOFF, DEFAULT_BACKOFF);

            if (ACTION_LOAD.equals(action)) {
                FetchDataHelper.loadData(this, new BackoffDelayAndRetryHandler(this, backoff));
            }
            else if (ACTION_LOAD_WAKEFUL.equals(action)) {
                try {
                    FetchDataHelper.loadData(this, new WakefulDelayAndRetryHandler(this, backoff));
                }
                finally {
                    // Always ensure wakelock released.
                    WakefulBroadcastReceiver.completeWakefulIntent(intent);
                }
            }
        }
        Log.d(TAG, "done");
    }

    static class BackoffDelayAndRetryHandler implements FetchDataHelper.DelayAndRetryHandler {

        private final Context applicationContext;
        private final long backoff;

        BackoffDelayAndRetryHandler(Context context, long backoff) {
            this.applicationContext = context.getApplicationContext();
            this.backoff = backoff;
        }

        public void delayAndRetry() {

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
                startService(applicationContext, newBackoff);
            }
            else {
                Log.d(TAG, "Hit max backoff, stopping");
            }
        }

        @Override
        public void delayAndRetryError() {
            delayAndRetry();
        }

        @Override
        public boolean isLoadCancelled() {
            return false;
        }
    }

    static class WakefulDelayAndRetryHandler implements FetchDataHelper.DelayAndRetryHandler {

        private final Context applicationContext;
        private final long backoff;

        WakefulDelayAndRetryHandler(Context context, long backoff) {
            applicationContext = context.getApplicationContext();
            this.backoff = backoff;
        }

        @Override
        public void delayAndRetry() {
            AlarmUtils.scheduleOneTimeAlarm(applicationContext, backoff);
        }

        @Override
        public void delayAndRetryError() {
            // do nothing
        }

        @Override
        public boolean isLoadCancelled() {
            return false;
        }
    }
}
