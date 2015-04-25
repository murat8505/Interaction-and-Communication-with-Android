package com.example.joeroger.sampleplaces.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


public class SearchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Should never get here, but if we do, just finish and return to the MainActivity
        Log.d("SearchActivity", "finishing...");
        finish();
    }
}
