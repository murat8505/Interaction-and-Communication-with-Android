package com.example.joeroger.homework2.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.joeroger.homework2.R;
import com.example.joeroger.homework2.fragment.AddCityFragment;
import com.example.joeroger.homework2.service.FavoriteCityService;
import com.google.android.gms.maps.model.LatLng;

public class AddCityActivity extends AppCompatActivity
        implements AddCityFragment.AddCityFragmentListener {

    public static Intent buildIntent(Context context) {
        return new Intent(context, AddCityActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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
    public void onAddCity(LatLng cityLocation) {
        FavoriteCityService.startAddFavoriteService(this, cityLocation);
        finish();
    }
}
