package com.example.bchan92.homework1.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;

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
    }

    @Override
    public void onDaySelected() {

    }
}
