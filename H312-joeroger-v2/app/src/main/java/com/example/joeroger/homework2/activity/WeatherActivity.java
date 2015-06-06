package com.example.joeroger.homework2.activity;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.joeroger.homework2.BuildConfig;
import com.example.joeroger.homework2.R;
import com.example.joeroger.homework2.adapter.CitySpinnerCursorAdapter;
import com.example.joeroger.homework2.fragment.ConditionsFragment;
import com.example.joeroger.homework2.fragment.DailyForecastFragment;
import com.example.joeroger.homework2.fragment.PlayServicesDialogFragment;
import com.example.joeroger.homework2.loader.CityConditionsLoaderCallbacks;
import com.example.joeroger.homework2.provider.CityConditionsContract;
import com.example.joeroger.homework2.receiver.FavoriteCityReceiver;
import com.example.joeroger.homework2.receiver.LocationSettingsReceiver;
import com.example.joeroger.homework2.service.FavoriteCityService;
import com.example.joeroger.homework2.service.LocationService;
import com.example.joeroger.homework2.service.WeatherService;
import com.example.joeroger.homework2.utils.AlarmUtils;
import com.example.joeroger.homework2.utils.JobUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class WeatherActivity extends AppCompatActivity
        implements Handler.Callback,
        ServiceConnection,
        CityConditionsLoaderCallbacks.OnCityConditionsLoaded,
        AdapterView.OnItemSelectedListener,
        PlayServicesDialogFragment.PlayServicesDialogFragmentListener,
        DailyForecastFragment.DailyForecastFragmentListener,
        View.OnClickListener,
        LocationSettingsReceiver.LocationSettingsListener,
        FavoriteCityReceiver.UpdateCityStatusListener {

    private static final String TAG = "WeatherActivity";
    private static final String STATE_RESOLVING_ERROR = "resolvingError";
    private static final String GPS_DIALOG_TAG = "GooglePlayServiceDialog";
    private static final int CHECK_SETTINGS_REQUEST = 100;

    private static final int SCHEDULE_JOB = 1;

    private static final int CITY_ID_POS = 0;
    private static final int FAVORITE_POS = 2;

    private int selectedPos;
    private long selectedCity;
    private long toggledCity;
    private boolean isToggledFavorite = false;
    private boolean isSelectedFavorite = true;

    private boolean resolvingError = false;
    private Handler handler;
    private LocationSettingsReceiver locationSettingsReceiver;
    private FavoriteCityReceiver favoriteCityReceiver;
    private LocationService.LocationServiceMgr locationServiceMgr;
    private CitySpinnerCursorAdapter citySpinnerCursorAdapter;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Turn on strict mode in debug builds
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }

        setContentView(R.layout.activity_weather);

        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            Spinner spinner = (Spinner) findViewById(R.id.city_spinner);
            citySpinnerCursorAdapter = new CitySpinnerCursorAdapter(actionBar.getThemedContext(), null);
            spinner.setAdapter(citySpinnerCursorAdapter);
            spinner.setOnItemSelectedListener(this);
            ViewCompat.setElevation(spinner, 0);
        }

        HandlerThread handlerThread = new HandlerThread("WeatherActivityThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper(), this);

        resolvingError = savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR);

        CityConditionsLoaderCallbacks.initLoader(getSupportLoaderManager(), this, this,
                CitySpinnerCursorAdapter.PROJECTION,
                CityConditionsContract.WHERE_CURRENT_OR_FAVORITE,
                CityConditionsContract.WHERE_CURRENT_OR_FAVORITE_ARGS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_add:
                startActivity(AddCityActivity.buildIntent(this));
                return true;
            case R.id.toggle_favorite:
                if (snackbar != null) snackbar.dismiss();
                toggledCity = selectedCity;
                isToggledFavorite = !isSelectedFavorite;
                FavoriteCityService.startToggleFavoriteService(this, toggledCity, isSelectedFavorite);
                return true;
            case R.id.action_settings:
                startActivity(SettingsActivity.buildIntent(this));
                return true;
            case R.id.action_open_source:
                startActivity(OpenSourceLicenseActivity.buildIntent(this));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationSettingsReceiver = new LocationSettingsReceiver(this, this);
        bindService(LocationService.buildIntent(this), this, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        favoriteCityReceiver = new FavoriteCityReceiver(this, this);

        // Checking for play services globally vs each individual way
        if (isGooglePlayServicesReady()) {
            Log.d(TAG, "Google play services available");
        }

        WeatherService.startActionFetch(this);
        handler.sendMessage(handler.obtainMessage(SCHEDULE_JOB));
    }

    @Override
    protected void onPause() {
        favoriteCityReceiver.unregister();
        super.onPause();
    }

    @Override
    protected void onStop() {
        unbindService(this);
        locationSettingsReceiver.unregister();
        locationServiceMgr = null;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        handler.getLooper().quit();
        handler = null;
        snackbar = null;
        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.toggle_favorite);
        item.setTitle(isSelectedFavorite ? R.string.remove_favorite : R.string.add_favorite);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the play services dialog result. This code is used whether the play
        // services dialog fragment was started by the fragment or the activity. Google
        // play services starts the activity such that only the activity is able to handle
        // the request code in onActivityResult.
        if (requestCode == PlayServicesDialogFragment.PLAY_SERVICES_DIALOG_REQUEST) {
            resolvingError = false;
            if (resultCode == Activity.RESULT_OK) {
                // Situation resolved. Can now activate GPS services
                Log.d(TAG, "Google play services available");

                if (locationServiceMgr != null) {
                    locationServiceMgr.onPlayServicesAreAvailable();
                }
            }
            else {
                // Update failed. Do something reasonable. Here leaving the app...
                Log.w(TAG, "Result not ok");
                noPlayServicesAvailable();
            }
        }
        else if (requestCode == CHECK_SETTINGS_REQUEST) {
            // Notify location fragment location has been resolved. Service, will just get locations..
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "User resolved settings request");
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected");
        locationServiceMgr = (LocationService.LocationServiceMgr) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected");
        locationServiceMgr = null;
    }

    @Override
    public void onCityConditionsLoaded(Cursor cursor) {
        citySpinnerCursorAdapter.swapCursor(cursor);
        if (cursor != null) {
            if (selectedPos < cursor.getCount()) {
                updateSelectionValues(selectedPos);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        // Id = cityId due to projection in adapter
        ConditionsFragment conditionsFragment = (ConditionsFragment)
                getSupportFragmentManager().findFragmentById(R.id.conditions_fragment);
        conditionsFragment.setCityId(id);

        DailyForecastFragment dailyForecastFragment = (DailyForecastFragment)
                getSupportFragmentManager().findFragmentById(R.id.daily_forecast_fragment);
        dailyForecastFragment.setCityId(id);

        updateSelectionValues(position);
    }

    private void updateSelectionValues(int position) {
        selectedPos = position;
        Cursor cursor = citySpinnerCursorAdapter.getCursor();
        cursor.moveToPosition(position);
        selectedCity = cursor.getLong(CITY_ID_POS);
        isSelectedFavorite = cursor.getInt(FAVORITE_POS) == 1;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        parent.setSelection(0);
    }

    @Override
    public void onDialogCancelled() {
        // User canceled the GPS dialog...
        Log.w(TAG, "Dialog cancelled");
        noPlayServicesAvailable();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case SCHEDULE_JOB:
                // Move jobs scheduling to another thread since it reads from shared
                // preferences to determine frequency user wants to have job run.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    JobUtils.scheduleJob(this);
                }
                else {
                    AlarmUtils.scheduleRecurringAlarm(this);
                }
                // Indicate handled message
                return true;
            default:
                // Not a message we are familiar with. Pass it along.
                return false;
        }
    }

    @Override
    public void onDaySelected(long cityId, long day) {
        startActivity(ForecastDetailActivity.buildIntent(this, cityId, day));
    }

    @Override
    public void onClick(View v) {
        FavoriteCityService.startToggleFavoriteService(this, toggledCity, isToggledFavorite);
        isToggledFavorite = !isToggledFavorite;
    }

    @Override
    public void onLocationSettingsResolutionNeeded(Status status) {
        if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
            // Location settings are not satisfied. But could be fixed by showing the user
            // a dialog.
            try {
                // Show the dialog by calling startResolutionForResult(),
                // and check the result in onActivityResult().
                status.startResolutionForResult(this, CHECK_SETTINGS_REQUEST);
            }
            catch (IntentSender.SendIntentException e) {
                // Ignore the error. Nothing we can do...
            }
        }
    }

    @Override
    public void onCityStatus(int statusMsgResId, boolean isRemoved) {
        snackbar = Snackbar.make(findViewById(R.id.root_layout), statusMsgResId, Snackbar.LENGTH_SHORT);
        if (isRemoved) {
            snackbar.setAction("Undo", this);
        }
        snackbar.show();
    }

    private void noPlayServicesAvailable() {
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
