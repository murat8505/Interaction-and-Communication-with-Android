package com.example.joeroger.homework2.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.joeroger.homework2.BuildConfig;
import com.example.joeroger.homework2.R;
import com.example.joeroger.homework2.adapter.CitySpinnerCursorAdapter;
import com.example.joeroger.homework2.fragment.ForecastFragment;
import com.example.joeroger.homework2.loader.CityConditionsLoaderCallbacks;

public class ForecastDetailActivity extends AppCompatActivity
implements CityConditionsLoaderCallbacks.OnCityConditionsLoaded {

    private static final String EXTRA_CITY_ID = BuildConfig.APPLICATION_ID + ".activity.ForecastDetailActivity.extra.cityId";
    private static final String EXTRA_DAY = BuildConfig.APPLICATION_ID + ".activity.ForecastDetailActivity.extra.day";

    private static final int CITY_NAME_POS = 1;

    public static Intent buildIntent(Context context, long cityId, long day) {
        Intent intent = new Intent(context, ForecastDetailActivity.class);
        intent.putExtra(EXTRA_CITY_ID, cityId);
        intent.putExtra(EXTRA_DAY, day);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast_detail);
        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Seattle Forecast");
        }

        long cityId = getIntent().getLongExtra(EXTRA_CITY_ID, 0);
        long day = getIntent().getLongExtra(EXTRA_DAY, 0);

        ForecastFragment forecastFragment = (ForecastFragment)
                getSupportFragmentManager().findFragmentById(R.id.forecast_fragment);
        forecastFragment.setCityIdAndDay(cityId, day);
        CityConditionsLoaderCallbacks.initLoader(getSupportLoaderManager(), this, this, CitySpinnerCursorAdapter.PROJECTION, cityId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent parentActivityIntent = NavUtils.getParentActivityIntent(this);
            parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            NavUtils.navigateUpTo(this, parentActivityIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCityConditionsLoaded(Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            String cityName = cursor.getString(CITY_NAME_POS);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(getString(R.string.title_activity_forecast_detail, cityName));
            }
        }
    }
}
