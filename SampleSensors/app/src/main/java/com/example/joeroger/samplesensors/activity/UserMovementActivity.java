package com.example.joeroger.samplesensors.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.joeroger.samplesensors.R;
import com.example.joeroger.samplesensors.fragment.PlayServicesDialogFragment;
import com.example.joeroger.samplesensors.fragment.UserMovementFragment;

public class UserMovementActivity extends AppCompatActivity
        implements UserMovementFragment.UserMovementFragmentListener {

    public static Intent buildIntent(Context context) {
        return new Intent(context, UserMovementActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_movement);
        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_activty_demo, menu);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PlayServicesDialogFragment.PLAY_SERVICES_DIALOG_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                UserMovementFragment fragment = (UserMovementFragment)
                        getSupportFragmentManager().findFragmentById(R.id.user_movement_fragment);
                if (fragment != null) {
                    fragment.onPlayServicesAreAvailable();
                }
            }
            else {
                // Update failed. Do something reasonable. Here leaving the app...
                onPlayServicesUnavailable();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPlayServicesUnavailable() {
        Toast.makeText(this, "Play Services are unavailable", Toast.LENGTH_SHORT).show();
        finish();
    }
}
