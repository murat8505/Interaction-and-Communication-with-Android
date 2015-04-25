package com.example.joeroger.sampleplaces.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.joeroger.sampleplaces.R;
import com.example.joeroger.sampleplaces.fragment.PlacesFragment;
import com.example.joeroger.sampleplaces.fragment.PlayServicesDialogFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

// With support library 22.1 use AppCompatActivity instead of ActionBarActivity
public class MainActivity extends AppCompatActivity
        implements PlayServicesDialogFragment.PlayServicesDialogFragmentListener,
        PlacesFragment.PlacesFragmentListener {

    private static final String TAG = "MainActivity";

    private static final String STATE_RESOLVING_ERROR = "resolvingError";
    private static final String GPS_DIALOG_TAG = "GooglePlayServiceDialog";
    private boolean resolvingError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));

        resolvingError = savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_license, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_license) {
            startActivity(LicenseActivity.buildIntent(this));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Uncomment this if, if you want to test using the activity check. However,
        // comment out the places fragment onConnectionFailed implementation or you
        // are showing two dialogs...

        //if (isGooglePlayServicesReady()) {
        //    Log.d(TAG, "Google play services available");
        // If need GPS here, you could start your "connection". Alternatively,
        // just connect to the api and handle the error that way. See
        // http://developer.android.com/google/auth/api-client.html
        // I tend to check up front in the app, unless I can successfully defer to a later
        // activity. This allows the app to avoid doing all of the error handling in
        // different fragments/services and I would put up a simple notification if
        // somehow it failed. This is highly unlikely unless the user manually uninstalls
        // the updates for google play services.
        //}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the play services dialog result. This code is used whether the play
        // services dialog fragment was started by the fragment or the activity. Google
        // play services starts the activity such that only the activity is able to handle
        // the request code in onActivityResult.
        if (requestCode == PlayServicesDialogFragment.PLAY_SERVICES_DIALOG_RESULT) {
            resolvingError = false;
            if (resultCode == Activity.RESULT_OK) {
                // Situation resolved. Can now activate GPS services
                Log.d(TAG, "Google play services available");

                // Notify all fragments that may have a connection to google play services.
                // I'd probably try to limit it to one fragment per activity to keep things
                // simple. In this example, need to notify the places fragment that play
                // services are available and to retry the connection.
                PlacesFragment placesFragment = (PlacesFragment) getSupportFragmentManager().findFragmentById(R.id.places_fragment);
                placesFragment.onPlayServicesAreAvailable();
            }
            else {
                // Update failed. Do something reasonable. Here leaving the app...
                Log.w(TAG, "Result not ok");
                noPlayServicesAvailable();
            }
        }
    }

    @Override
    public void onDialogCancelled() {
        // User canceled the GPS dialog...
        Log.w(TAG, "Dialog cancelled");
        noPlayServicesAvailable();
    }

    @Override
    public void startImageActivity() {
        startActivity(ImageActivity.buildIntent(this));
    }

    @Override
    public void noPlayServicesAvailable() {
        Toast.makeText(this, R.string.play_services_error, Toast.LENGTH_LONG).show();
        finish();
    }

    private boolean isGooglePlayServicesReady() {
        // If already showing dialog, then skip further checks.
        if (resolvingError) return false;

        // See if play services is available
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (result == ConnectionResult.SUCCESS) {
            return true;
        }
        else {
            DialogFragment fragment = PlayServicesDialogFragment.newInstance(result);
            fragment.show(getSupportFragmentManager(), GPS_DIALOG_TAG);
            resolvingError = true;
            return false;
        }
    }
}
