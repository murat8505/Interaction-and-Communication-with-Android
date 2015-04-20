package com.example.bchan92.homework1.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.example.bchan92.homework1.R;
import com.example.bchan92.homework1.fragment.ForecastListFragment;

public class ForecastActivity extends ActionBarActivity
    implements ForecastListFragment.ForecastFragmentListener {

    public static Intent buildIntent(Context context) {
        return new Intent(context, ForecastActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        Toolbar toolbar = (Toolbar) findViewById(R.id.forecastToolBar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setCurrentConditions();
    }

    private void setCurrentConditions() {
        TextView current_conditions = (TextView) findViewById(R.id.current_condition_status);
        TextView current_temp = (TextView) findViewById(R.id.current_temp_text);
        TextView humidity = (TextView) findViewById(R.id.humidity_text);
        TextView wind_speed = (TextView) findViewById(R.id.wind_speed_text);

    }

    @Override
    public void onDaySelected() {

    }
}
