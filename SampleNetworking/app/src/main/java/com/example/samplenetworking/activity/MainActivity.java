package com.example.samplenetworking.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.example.samplenetworking.R;
import com.example.samplenetworking.service.NetworkIntentService;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));
        getSupportActionBar().setElevation(getResources().getDimensionPixelSize(R.dimen.actionbar_elevation));
        NetworkIntentService.startService(this);
    }


}
