package com.example.joeroger.samplesearchshare.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.example.joeroger.samplesearchshare.R;
import com.example.joeroger.samplesearchshare.fragment.ContactDetailFragment;
import com.example.joeroger.samplesearchshare.provider.ContactContract;

public class ContactDetailActivity extends ActionBarActivity {

    private static final String EXTRA_CONTACT_ID = ContactDetailActivity.class.getName() + ".id";

    public static Intent buildIntent(Context context, long contactId) {
        Intent intent = new Intent(context, ContactDetailActivity.class);
        intent.putExtra(EXTRA_CONTACT_ID, contactId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Must be phone, so load our hierarchy now.
        setContentView(R.layout.activity_contact_detail);

        setSupportActionBar((Toolbar) findViewById(R.id.contactDetailToolBar));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Only add a dynamic fragment if the saved instance state is null.
        // Otherwise it already has been added to the view hierarchy
        if (savedInstanceState == null) {
            long contactId;

            // When coming from a search suggestion, the contact id will be in the "EXTRA_DATA_KEY"
            // based on how I setup the content provider.
            if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
                String contactIdString = getIntent().getStringExtra(SearchManager.EXTRA_DATA_KEY);
                if (TextUtils.isEmpty(contactIdString)) {
                    finish();
                    return;
                }
                try {
                    contactId = Long.valueOf(contactIdString);
                }
                catch (NumberFormatException e) {
                    finish();
                    return;
                }
            }
            else {
                contactId = getIntent().getLongExtra(EXTRA_CONTACT_ID, ContactContract.NO_CONTACT_ID);
            }

            ContactDetailFragment fragment = ContactDetailFragment.newInstance(contactId);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.contactDetailContainer, fragment, "DETAILS")
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
