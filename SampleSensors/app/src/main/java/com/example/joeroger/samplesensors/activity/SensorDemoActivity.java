package com.example.joeroger.samplesensors.activity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.joeroger.samplesensors.BuildConfig;
import com.example.joeroger.samplesensors.R;
import com.example.joeroger.samplesensors.fragment.SensorDemoFragment;


public class SensorDemoActivity extends AppCompatActivity {

    private static final String EXTRA_SENSOR_TYPE = BuildConfig.APPLICATION_ID + ".activity.SensorDemoActivity.extra.type";

    public static Intent buildIntent(Context context, int sensorType) {
        Intent intent = new Intent(context, SensorDemoActivity.class);
        intent.putExtra(EXTRA_SENSOR_TYPE, sensorType);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_demo);
        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        SensorDemoFragment fragment = (SensorDemoFragment) getSupportFragmentManager().findFragmentById(R.id.sensor_demo_fragment);
        fragment.setSensorType(getIntent().getIntExtra(EXTRA_SENSOR_TYPE, Sensor.TYPE_ACCELEROMETER));
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
}
