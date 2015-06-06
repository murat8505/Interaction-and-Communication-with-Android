package com.example.joeroger.homework1.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.example.joeroger.BuildConfig;
import com.example.joeroger.R;
import com.example.joeroger.homework1.fragment.ForecastFragment;

public class ForecastDetailActivity extends ActionBarActivity {

    private static final String EXTRA_CITY_ID = BuildConfig.APPLICATION_ID + ".activity.ForecastDetailActivity.extra.cityId";
    private static final String EXTRA_DAY = BuildConfig.APPLICATION_ID + ".activity.ForecastDetailActivity.extra.day";

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Seattle Forecast");

        long cityId = getIntent().getLongExtra(EXTRA_CITY_ID, 0);
        long day = getIntent().getLongExtra(EXTRA_DAY, 0);

        ForecastFragment forecastFragment = (ForecastFragment)
                getSupportFragmentManager().findFragmentById(R.id.forecast_fragment);
        forecastFragment.setCityIdAndDay(cityId, day);
    }

}
