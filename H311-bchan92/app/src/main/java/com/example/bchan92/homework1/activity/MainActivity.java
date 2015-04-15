package com.example.bchan92.homework1.activity;

import android.content.Intent;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.bchan92.homework1.R;

public class MainActivity extends ActionBarActivity
        implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.topLevelToolBar);
        setSupportActionBar(toolbar);
        findViewById(R.id.forecast_button).setOnClickListener(this);

//        setSupportActionBar((Toolbar) findViewById(R.id.topLevelToolBar));
//
//        // Disable the default title
//        getSupportActionBar().setDisplayShowTitleEnabled(false);

//        // Setup the navigation spinner
//        Spinner spinner = (Spinner) findViewById(R.id.spinner);
//
//        // Use action bar's themed context for spinner
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                // Using themed context to ensure text is white...
//                getSupportActionBar().getThemedContext(),
//                R.layout.item_spinner_title,
//                android.R.id.text1,
//                getResources().getStringArray(R.array.cities));
//
//        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
//        spinner.setAdapter(adapter);
//        //spinner.setOnItemSelectedListener(this);
//
//        spinner.setSelection(0);
//
//        // If toolbar elevated, then the spinner also tries to be elevated.
//        // This looks ugly on Android 5.0+. Set elevation to 0 to prevent
//        ViewCompat.setElevation(spinner, 0);
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
        if (id == R.id.add_city) {
            return true;
        }
//        else if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        Intent intent = null;
        switch (id) {
            case R.id.forecast_button:
                intent = ForecastActivity.buildIntent(this);
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
