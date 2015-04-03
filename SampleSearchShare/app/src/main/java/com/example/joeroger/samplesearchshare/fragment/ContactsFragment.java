package com.example.joeroger.samplesearchshare.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.joeroger.samplesearchshare.R;
import com.example.joeroger.samplesearchshare.activity.AddContactActivity;
import com.example.joeroger.samplesearchshare.activity.ContactDetailActivity;
import com.example.joeroger.samplesearchshare.provider.ContactContract;

/**
 * Master/Detail fragment for Contacts
 */
public class ContactsFragment extends Fragment
        implements ContactListFragment.ContactListFragmentListener {

    private static final String LIST_FRAG = "contactListFrag";
    private static final String DETAIL_FRAG = "contactDetailFrag";
    private static final String STATE_HAD_DETAIL_FRAGMENT = ContactsFragment.class.getName() + ".hadDetailFragment";
    private static final String STATE_SELECTED_CONTACT_ID = ContactsFragment.class.getName() + ".contactId";

    private boolean hasDetailFragment = false;
    private boolean hadDetailFragment = false;
    private long selectedContactId = ContactContract.NO_CONTACT_ID;

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    public ContactsFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Must call this in onCreate to see action bar item
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hasDetailFragment = view.findViewById(R.id.contactDetailContainer) != null;

        if (savedInstanceState == null) {
            ContactListFragment contactListFragment = ContactListFragment.newInstance(hasDetailFragment);
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.contactListContainer, contactListFragment, LIST_FRAG)
                    .commit();
        }
        else {
            selectedContactId = savedInstanceState.getLong(STATE_SELECTED_CONTACT_ID, ContactContract.NO_CONTACT_ID);
            hadDetailFragment = savedInstanceState.getBoolean(STATE_HAD_DETAIL_FRAGMENT, false);

            ContactListFragment contactsListFragment = (ContactListFragment)
                    getChildFragmentManager().findFragmentByTag(LIST_FRAG);
            contactsListFragment.setHighlightList(hasDetailFragment);
        }

        // Ensure detail frag shown on Nexus 7
        if (hasDetailFragment && selectedContactId != ContactContract.NO_CONTACT_ID) {
            if (!hadDetailFragment) {
                onContactListInitialized(selectedContactId);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Adding add here since this fragment will always show the child. Also by placing
        // it here it limits the interaction with list fragment to just notifying
        // of the new item to add to the data set
        inflater.inflate(R.menu.menu_fragment_contacts, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        switch (item.getItemId()) {
            case R.id.add_contact:
                startActivity(AddContactActivity.buildIntent(getActivity()));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_SELECTED_CONTACT_ID, selectedContactId);
        outState.putBoolean(STATE_HAD_DETAIL_FRAGMENT, hasDetailFragment);
    }

    @Override
    public void onContactListInitialized(long contactId) {
        if (hasDetailFragment) {
            ContactDetailFragment contactDetailFragment = (ContactDetailFragment)
                    getChildFragmentManager().findFragmentByTag(DETAIL_FRAG);
            if (contactDetailFragment == null) {
                contactDetailFragment = ContactDetailFragment.newInstance(contactId);
                getChildFragmentManager()
                        .beginTransaction()
                        .add(R.id.contactDetailContainer, contactDetailFragment, DETAIL_FRAG)
                        .commit();
            }
        }
    }

    @Override
    public void onContactListItemSelected(long contactId) {
        if (hasDetailFragment) {
            ContactDetailFragment contactDetailFragment = ContactDetailFragment.newInstance(contactId);
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contactDetailContainer, contactDetailFragment, DETAIL_FRAG)
                    .commit();
        }
        else {
            startActivity(ContactDetailActivity.buildIntent(getActivity(), contactId));
        }
    }
}
