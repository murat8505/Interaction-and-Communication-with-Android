package com.example.joeroger.homework2.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.joeroger.homework2.R;
import com.example.joeroger.homework2.fragment.OpenSourceLicenseFragment;

public class OpenSourceLicenseActivity extends AppCompatActivity
    implements OpenSourceLicenseFragment.OpenSourceLicenseFragmentListener {

    public static Intent buildIntent(Context context) {
        return new Intent(context, OpenSourceLicenseActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_source_license);
        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_content, OpenSourceLicenseFragment.newInstance(), "TAG")
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
    public void showLicenseFragment(Fragment licenseFragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, licenseFragment, "LICENSE")
                .addToBackStack("LICENSE")
                .commit();
    }
}
