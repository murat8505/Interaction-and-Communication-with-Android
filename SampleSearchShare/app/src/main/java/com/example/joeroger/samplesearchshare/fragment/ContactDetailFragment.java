package com.example.joeroger.samplesearchshare.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.joeroger.samplesearchshare.R;
import com.example.joeroger.samplesearchshare.loader.ContactLoaderCallbacks;
import com.example.joeroger.samplesearchshare.provider.ContactContract;
import com.example.joeroger.samplesearchshare.service.ContactIntentService;

import java.util.Date;


public class ContactDetailFragment extends Fragment
        implements DatePickerDialogFragment.DatePickerDialogFragmentListener,
        View.OnClickListener,
        ContactLoaderCallbacks.ContactLoadListener {

    private static final String ARG_ID = ContactDetailFragment.class.getName() + ".id";

    private static final int BIRTH_REQUEST_CODE = 100;

    private static final String[] PROJECTION = new String[]{
            ContactContract.Columns.NAME,
            ContactContract.Columns.ADDRESS,
            ContactContract.Columns.BIRTH_DATE
    };

    private static final int NAME_POS = 0;
    private static final int ADDRESS_POS = 1;
    private static final int BIRTH_DATE_POS = 2;

    private ShareActionProvider shareActionProvider;
    private long contactId;
    private String name;
    private String address = "";
    private Date birthDate = new Date();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param contactId the contact id
     * @return A new instance of fragment ContactDetailFragment.
     */
    public static ContactDetailFragment newInstance(long contactId) {
        ContactDetailFragment fragment = new ContactDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ID, contactId);
        fragment.setArguments(args);
        return fragment;
    }

    public ContactDetailFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            contactId = getArguments().getLong(ARG_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contact_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        holder.birthDateView.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ContactLoaderCallbacks.initLoader(getActivity(), getLoaderManager(), this, PROJECTION, contactId);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Adding add here since this fragment will always show the child. Also by placing
        // it here it limits the interaction with list fragment to just notifying
        // of the new item to add to the data set
        inflater.inflate(R.menu.menu_fragment_contact_details, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.share);

        // Fetch and store ShareActionProvider, so we can update share intent later.
        // Releasing reference in onDestroyOptionsMenu
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (name != null) {
            updateShareIntent();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ViewHolder holder = getViewHolder();
        // holder better not be null or there is a bug earlier in the code.
        // onResume always will have a view.

        // Refresh our views. Primarily because we will know our model is restored
        // if we had saved state by this point.
        //
        // Also side benefit is if user changes date format via settings
        // and comes back to app we will honor the new format. Try it...
        updateView(holder);
    }

    @Override
    public void onPause() {
        ViewHolder holder = getViewHolder();
        // holder should not be null as this method is called on UI thread and before
        // destroy view.

        // Update model value for the address so we can save it later in onSaveInstanceState
        address = holder.addressView.getText().toString();
        ContentValues values = new ContentValues();
        values.put(ContactContract.Columns.ADDRESS, address);
        values.put(ContactContract.Columns.BIRTH_DATE, birthDate.getTime());
        ContactIntentService.startUpdate(getActivity(), contactId, values);
        super.onPause();
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        shareActionProvider = null;
    }

    private void updateView(ViewHolder holder) {
        if (holder == null) {
            return;
        }

        holder.nameView.setText(name);
        holder.addressView.setText(address);
        updateBirthDateView(holder.birthDateView, birthDate);
    }

    private void updateBirthDateView(TextView birthDateView, Date newDate) {
        // This is using the user's preferred date format and locale info to
        // format the date. Always best to show date/time in user's preferred format.

        // If also showing time there is a getTimeFormat() method as well
        birthDateView.setText(DateFormat.getDateFormat(getActivity()).format(newDate));
    }

    @Override
    public void onDateSet(int requestId, @NonNull Date date) {
        if (requestId == BIRTH_REQUEST_CODE) {
            // update our model...
            birthDate = date;

            // update the view as well...
            ViewHolder holder = getViewHolder();
            if (holder != null) {
                updateBirthDateView(holder.birthDateView, birthDate);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.birthDate) {
            DatePickerDialogFragment fragment = DatePickerDialogFragment.newInstance(BIRTH_REQUEST_CODE, birthDate);
            fragment.show(getChildFragmentManager(), "DIALOG");
        }
    }

    @Override
    public void onContactLoadComplete(Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            name = cursor.getString(NAME_POS);
            address = cursor.getString(ADDRESS_POS);
            birthDate = new Date(cursor.getLong(BIRTH_DATE_POS));
            updateView(getViewHolder());
            updateShareIntent();
        }
    }

    /**
     * Helper to build the "share" intent providing a greeting and the name of the user
     */
    private void updateShareIntent() {
        // If provider doesn't exist, then nothing to do so get out.
        if (shareActionProvider == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Hello, " + name);
        shareActionProvider.setShareIntent(intent);
    }

    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    static class ViewHolder {
        final TextView nameView;
        final EditText addressView;
        final TextView birthDateView;

        ViewHolder(View view) {
            nameView = (TextView) view.findViewById(R.id.name);
            addressView = (EditText) view.findViewById(R.id.address);
            birthDateView = (TextView) view.findViewById(R.id.birthDate);
        }
    }
}
