package com.example.joeroger.samplesensors.activity;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.joeroger.samplesensors.R;


public class MainActivity extends AppCompatActivity
implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));

        findViewById(R.id.user_movement_button).setOnClickListener(this);
        findViewById(R.id.available_sensors_button).setOnClickListener(this);

        View ambiantTempButton = findViewById(R.id.ambiant_temperature_button);
        ambiantTempButton.setOnClickListener(this);

        SensorManager sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        ambiantTempButton.setVisibility(sensor != null ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.user_movement_button:
                startActivity(UserMovementActivity.buildIntent(this));
                break;
            case R.id.available_sensors_button:
                startActivity(SensorListActivity.buildIntent(this));
                break;
            case R.id.ambiant_temperature_button:
                startActivity(NoSensorActivity.buildIntent(this));
                break;
        }
    }
}
