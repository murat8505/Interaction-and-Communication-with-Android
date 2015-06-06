package com.example.joeroger.samplemaps.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.joeroger.samplemaps.BuildConfig;
import com.example.joeroger.samplemaps.R;
import com.example.joeroger.samplemaps.fragment.IncidentDetailFragment;
import com.example.joeroger.samplemaps.fragment.IncidentDetailMapFragment;
import com.example.joeroger.samplemaps.provider.Seattle911Contract;
import com.google.android.gms.maps.model.LatLng;

public class IncidentDetailActivity extends AppCompatActivity {

    private static final String EXTRA_ID = IncidentDetailActivity.class.getName() + ".extra.id";
    private static final String EXTRA_POSITION = IncidentDetailActivity.class.getName() + ".extra.position";

    public static Intent buildIntent(Context context, long incidentId, LatLng position) {
        Intent intent = new Intent(context, IncidentDetailActivity.class);
        intent.putExtra(EXTRA_ID, incidentId);
        intent.putExtra(EXTRA_POSITION, position);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_detail);
        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        long incidentId = getIntent().getLongExtra(EXTRA_ID, Seattle911Contract.NO_INCIDENT_ID);
        LatLng position = getIntent().getParcelableExtra(EXTRA_POSITION);

        if (savedInstanceState == null && incidentId != Seattle911Contract.NO_INCIDENT_ID) {
            IncidentDetailMapFragment mapFragment = (IncidentDetailMapFragment)
                    getSupportFragmentManager().findFragmentById(R.id.incident_detail_map_fragment);

            mapFragment.setIncidentData(incidentId, position);

            IncidentDetailFragment detailFragment = (IncidentDetailFragment)
                    getSupportFragmentManager().findFragmentById(R.id.incident_detail_fragment);
            detailFragment.setIncidentId(incidentId);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                // Don't restart activity...
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
