package com.example.joeroger.samplelocation.activity;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.example.joeroger.samplelocation.BuildConfig;
import com.example.joeroger.samplelocation.R;
import com.example.joeroger.samplelocation.fragment.GPSLocationFragment;
import com.example.joeroger.samplelocation.fragment.GeoCodeFragment;
import com.example.joeroger.samplelocation.fragment.IncidentListFragment;
import com.example.joeroger.samplelocation.fragment.LocationFragmentListener;
import com.example.joeroger.samplelocation.fragment.PlayServicesDialogFragment;
import com.example.joeroger.samplelocation.network.AlarmUtils;
import com.example.joeroger.samplelocation.network.ConnectivityUtils;
import com.example.joeroger.samplelocation.network.JobUtils;
import com.example.joeroger.samplelocation.receiver.ConnectivityBroadcastReceiver;
import com.example.joeroger.samplelocation.service.NetworkIntentService;

public class MainActivity extends AppCompatActivity
        implements LocationFragmentListener {

    private static final String TAG = "MainActivity";
    private static final String LOCATION_FRAG = "LocationFrag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Starting main activity");

        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));
        setTitle(R.string.title_activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(getResources().getDimensionPixelSize(R.dimen.actionbar_elevation));
        }

        if (savedInstanceState == null) {
            // Choose one or the other fragment. Android uses only OS services to do the work.
            // GooglePlayServices requires google services to do the work.

//            getSupportFragmentManager().beginTransaction()
//                    .add(AndroidLocationFragment.newInstance(), LOCATION_FRAG)
//                    .commit();

            getSupportFragmentManager().beginTransaction()
                    .add(GPSLocationFragment.newInstance(), LOCATION_FRAG)
                    .commit();
        }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the play services dialog result. This code is used whether the play
        // services dialog fragment was started by the fragment or the activity. Google
        // play services starts the activity such that only the activity is able to handle
        // the request code in onActivityResult.
        if (requestCode == PlayServicesDialogFragment.PLAY_SERVICES_DIALOG_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                // Situation resolved. Can now activate GPS services
                Log.d(TAG, "Google play services available");

                // Notify all fragments that may have a connection to google play services.
                // I'd probably try to limit it to one fragment per activity to keep things
                // simple. In this example, need to notify the places fragment that play
                // services are available and to retry the connection.
                GPSLocationFragment locationFragment = (GPSLocationFragment) getSupportFragmentManager().findFragmentByTag(LOCATION_FRAG);
                locationFragment.onPlayServicesAreAvailable();
            }
            else {
                // Update failed. Do something reasonable. Here leaving the app...
                Log.w(TAG, "Result not ok");
                onPlayServicesUnavailable();
            }
        }
        else if (requestCode == GPSLocationFragment.CHECK_SETTINGS_REQUEST) {
            GPSLocationFragment locationFragment = (GPSLocationFragment) getSupportFragmentManager().findFragmentByTag(LOCATION_FRAG);

            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "User resolved settings request");
                locationFragment.onSettingsEnabled();
            }
            else {
                Log.d(TAG, "User did not enable location settings");
                locationFragment.onSettingsNotEnabled();
            }
        }
    }

    @Override
    public void onNewLocation(Location location) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "New location: " + location);
        }

        if (location == null) return;

        IncidentListFragment fragment = (IncidentListFragment) getSupportFragmentManager().findFragmentById(R.id.incident_fragment);
        if (fragment != null) {
            fragment.setLocation(location);
        }

        GeoCodeFragment geoCodeFragment = (GeoCodeFragment) getSupportFragmentManager().findFragmentById(R.id.geocode_fragment);
        if (geoCodeFragment != null) {
            geoCodeFragment.setLocation(location);
        }
    }

    @Override
    public void onPlayServicesUnavailable() {
        Toast.makeText(this, R.string.play_services_error, Toast.LENGTH_LONG).show();
        finish();
    }
}
