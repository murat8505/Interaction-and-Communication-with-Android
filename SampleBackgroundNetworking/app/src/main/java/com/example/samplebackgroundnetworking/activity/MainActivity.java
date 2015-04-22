package com.example.samplebackgroundnetworking.activity;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.example.samplebackgroundnetworking.R;
import com.example.samplebackgroundnetworking.network.AlarmUtils;
import com.example.samplebackgroundnetworking.network.ConnectivityUtils;
import com.example.samplebackgroundnetworking.network.JobUtils;
import com.example.samplebackgroundnetworking.receiver.ConnectivityBroadcastReceiver;
import com.example.samplebackgroundnetworking.service.NetworkIntentService;
import com.example.samplebackgroundnetworking.service.NetworkJobService;

import java.util.List;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Starting main activity");

        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));
        getSupportActionBar().setElevation(getResources().getDimensionPixelSize(R.dimen.actionbar_elevation));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobUtils.scheduleJob(this);
        }
        else {
            AlarmUtils.scheduleRecurringAlarm(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh our data if connected.
        if (ConnectivityUtils.isConnected(this)) {
            NetworkIntentService.startService(this);
        }
        // If not connected, enable the connectivity receiver to try as soon as
        // connectivity is reestablished.
        else {
            Log.d(TAG, "Enabling connectivity receiver...");
            ConnectivityBroadcastReceiver.enableReceiver(this, true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // User leaving app, so stop monitoring for a connection. Do this
        // always, as only want it on in certain situations.
        ConnectivityBroadcastReceiver.enableReceiver(this, false);
    }
}
