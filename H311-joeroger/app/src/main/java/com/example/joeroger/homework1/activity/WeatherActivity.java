package com.example.joeroger.homework1.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.example.joeroger.R;
import com.example.joeroger.homework1.fragment.ConditionsFragment;
import com.example.joeroger.homework1.fragment.DailyForecastFragment;
import com.example.joeroger.homework1.service.WeatherService;

public class WeatherActivity extends ActionBarActivity
        implements DailyForecastFragment.DailyForecastFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));
        getSupportActionBar().setTitle("Seattle");

        // HW1, hardcoding seattle..., HW2 implement city chooser
        ConditionsFragment conditionsFragment = (ConditionsFragment)
                getSupportFragmentManager().findFragmentById(R.id.conditions_fragment);
        conditionsFragment.setCityId(5809844);

        DailyForecastFragment dailyForecastFragment = (DailyForecastFragment)
                getSupportFragmentManager().findFragmentById(R.id.daily_forecast_fragment);
        dailyForecastFragment.setCityId(5809844);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WeatherService.startActionLoad(this);
    }

    @Override
    public void onDaySelected(long cityId, long day) {
        startActivity(ForecastDetailActivity.buildIntent(this, cityId, day));
    }
}
