package com.example.joeroger.samplebackgrounding.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.example.joeroger.samplebackgrounding.network.AlarmUtils;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class NetworkAlarmIntentService extends IntentService {
    private static final String TAG = "NetworkAlarmService";

    private static final String ACTION_LOAD_WAKEFUL = NetworkAlarmIntentService.class.getName() + ".action.loadWakeful";
    private static final String EXTRA_BACKOFF = NetworkAlarmIntentService.class.getName() + ".extra.backoff";

    private static final long DEFAULT_BACKOFF = 500;

    public static Intent buildWakefulIntent(Context context, long backoff) {
        Intent intent = new Intent(context, NetworkAlarmIntentService.class);
        intent.setAction(ACTION_LOAD_WAKEFUL);
        intent.putExtra(EXTRA_BACKOFF, backoff);
        return intent;
    }

    public NetworkAlarmIntentService() {
        super("NetworkAlarmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "starting");
        if (intent != null) {
            final String action = intent.getAction();
            long backoff = intent.getLongExtra(EXTRA_BACKOFF, DEFAULT_BACKOFF);


            if (ACTION_LOAD_WAKEFUL.equals(action)) {
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
