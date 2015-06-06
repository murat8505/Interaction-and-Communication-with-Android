package com.example.joeroger.samplebackgrounding.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.example.joeroger.samplebackgrounding.BuildConfig;
import com.example.joeroger.samplebackgrounding.R;
import com.example.joeroger.samplebackgrounding.fragment.GPSLocationFragment;
import com.example.joeroger.samplebackgrounding.fragment.GeoCodeFragment;
import com.example.joeroger.samplebackgrounding.fragment.IncidentListFragment;
import com.example.joeroger.samplebackgrounding.fragment.LocationFragmentListener;
import com.example.joeroger.samplebackgrounding.fragment.PlayServicesDialogFragment;
import com.example.joeroger.samplebackgrounding.network.AlarmUtils;
import com.example.joeroger.samplebackgrounding.network.ConnectivityUtils;
import com.example.joeroger.samplebackgrounding.network.JobUtils;
import com.example.joeroger.samplebackgrounding.receiver.ConnectivityBroadcastReceiver;
import com.example.joeroger.samplebackgrounding.service.LocationService;
import com.example.joeroger.samplebackgrounding.service.LocationServiceMgr;
import com.example.joeroger.samplebackgrounding.service.NetworkIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity
        implements LocationFragmentListener,
        ServiceConnection,
        LocationService.LocationServiceListener,
        PlayServicesDialogFragment.PlayServicesDialogFragmentListener {

    private static final String TAG = "MainActivity";
    private static final String LOCATION_FRAG = "LocationFrag";
    private static final String GPS_DIALOG_TAG = "GooglePlayServiceDialog";

    private static final String STATE_RESOLVING_ERROR = "resolvingError";

    private static final int RESOLVE_ERROR_REQUEST = 110;

    private LocationServiceMgr locationServiceMgr;
    private boolean resolvingError = false;
    private boolean isResumed = false;

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

            // If bound service is configured to be active, both fragments should be disabled.

//            getSupportFragmentManager().beginTransaction()
//                    .add(AndroidLocationFragment.newInstance(), LOCATION_FRAG)
//                    .commit();

//            getSupportFragmentManager().beginTransaction()
//                    .add(GPSLocationFragment.newInstance(), LOCATION_FRAG)
//                    .commit();
        }

        resolvingError = savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR);

        boolean useJob = getResources().getBoolean(R.bool.use_job_service);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && useJob) {
            JobUtils.scheduleJob(this);
        }
        else {
            AlarmUtils.scheduleRecurringAlarm(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // BIND_AUTO_CREATE will create the service as long as a client is bound to the service.
        // Unless the app is manually starting the service via startService() as well, this
        // flag is required for the service to start.
        if (bindService(LocationService.buildIntent(this), this, Context.BIND_AUTO_CREATE)) {
            Log.d(TAG, "Binding service");
        }
        else {
            Log.w(TAG, "Failed to bind to service");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        isResumed = true;
        if (locationServiceMgr != null) locationServiceMgr.registerListener(this);
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
        // User leaving app, so stop monitoring for a connection. Do this
        // always, as only want it on in certain situations.
        ConnectivityBroadcastReceiver.enableReceiver(this, false);

        isResumed = false;
        if (locationServiceMgr != null) locationServiceMgr.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        locationServiceMgr = null;
        unbindService(this);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, resolvingError);
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

            // Notify location fragment location has been resolved. Service, will just get locations..
            if (locationFragment != null) {
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
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        locationServiceMgr = (LocationServiceMgr) service;

        if (isResumed && locationServiceMgr != null) {
            locationServiceMgr.registerListener(this);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        locationServiceMgr = null;
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

    @Override
    public void onLocationUpdate(Location location) {
        // feed into fragment handling... vs duplicating code
        onNewLocation(location);
    }

    @Override
    public void resolveGPSError(ConnectionResult connectionResult) {
        if (resolvingError) {
            // Already attempting to resolve an error.
            return;
        }

        if (connectionResult.hasResolution()) {
            try {
                resolvingError = true;
                connectionResult.startResolutionForResult(this, RESOLVE_ERROR_REQUEST);
            }
            catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                if (locationServiceMgr != null) locationServiceMgr.onPlayServicesAreAvailable();
                resolvingError = false;
            }
        }
        else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            // Must use your own fragment wrapper around the dialog or rotation does not work correctly.
            // Luckily you could essentially copy the PlayServicesDialogFragment from project to project.
            // Works with both activities/fragments as parent. However, the onActivityResult() is always
            // routed to the "Activity". So the activity is responsible for notifying any fragments that
            // google play services are available.
            DialogFragment fragment = PlayServicesDialogFragment.newInstance(connectionResult.getErrorCode());
            fragment.show(getSupportFragmentManager(), GPS_DIALOG_TAG);
            resolvingError = true;
        }
    }

    @Override
    public void resolveLocationSettingsError(Status status) {
        // Show the dialog by calling startResolutionForResult(),
        // and check the result in onActivityResult().
        // Using location fragment's request id. Mostly to avoid creating a 3rd request id
        // as the service will just get location updates if it works.
        try {
            status.startResolutionForResult(this, GPSLocationFragment.CHECK_SETTINGS_REQUEST);
        }
        catch (IntentSender.SendIntentException e) {
            Log.w(TAG, "Failed to start settings resolution");
        }
    }

    @Override
    public void onDialogCancelled() {
        onPlayServicesUnavailable();
    }
}
