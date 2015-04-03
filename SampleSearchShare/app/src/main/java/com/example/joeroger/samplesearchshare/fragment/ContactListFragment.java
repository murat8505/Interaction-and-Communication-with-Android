package com.example.joeroger.samplesearchshare.fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.joeroger.samplesearchshare.R;
import com.example.joeroger.samplesearchshare.activity.ContactDetailActivity;
import com.example.joeroger.samplesearchshare.adapter.ContactAdapter;
import com.example.joeroger.samplesearchshare.loader.ContactLoaderCallbacks;


public class ContactListFragment extends Fragment
        implements AdapterView.OnItemClickListener,
        ContactLoaderCallbacks.ContactLoadListener,
        SearchView.OnQueryTextListener {

    private static final String ARGS_CHOICE_MODE = "listChoiceMode";
    private static final String STATE_SELECTION = ContactListFragment.class.getSimpleName() + ".selection";


    private int listChoiceMode = ListView.CHOICE_MODE_NONE;
    private boolean notifyOnFirstLoad = true;
    private int selectedItem = 0;
    private ContactListFragmentListener listener;

    public interface ContactListFragmentListener {
        public void onContactListInitialized(long contactId);

        public void onContactListItemSelected(long contactId);
    }

    public static ContactListFragment newInstance(boolean highlightList) {
        ContactListFragment contactListFragment = new ContactListFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_CHOICE_MODE, highlightList ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
        contactListFragment.setArguments(args);
        return contactListFragment;
    }

    public ContactListFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        if (args != null) {
            listChoiceMode = args.getInt(ARGS_CHOICE_MODE, ListView.CHOICE_MODE_NONE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;
        try {
            listener = (ContactListFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName()
                    + " must implement ContactListFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // View holder here is not necessarily required since not accessing the
        // list view after this method. But including for example purposes
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        holder.list.setAdapter(new ContactAdapter(view.getContext()));
        holder.list.setOnItemClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ContactLoaderCallbacks.initLoader(getActivity(), getLoaderManager(), this, ContactAdapter.PROJECTION);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            selectedItem = savedInstanceState.getInt(STATE_SELECTION, -1);
        }

        configureList(getViewHolder().list);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Adding add here since this fragment will always show the child. Also by placing
        // it here it limits the interaction with list fragment to just notifying
        // of the new item to add to the data set
        inflater.inflate(R.menu.menu_fragment_contact_list, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        // You need to reference the activity that will handle the "search" result. In this case
        // I'm sending the result to the detail activity so building a component name to use
        // to look up the searchable info
        ComponentName componentName = new ComponentName(getActivity(), ContactDetailActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        // Adding this fragment as a listener, (variations on solution).
        searchView.setOnQueryTextListener(this);
    }

    public void setHighlightList(boolean highlightList) {
        this.listChoiceMode = highlightList ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE;
        ViewHolder holder = getViewHolder();
        if (holder != null) {
            configureList(holder.list);
        }
    }

    private void configureList(final ListView list) {
        list.setChoiceMode(listChoiceMode);
        if (list.getCount() > selectedItem) {
            list.setSelection(selectedItem);
            list.setItemChecked(selectedItem, true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTION, selectedItem);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedItem = position;
        ListView list = (ListView) parent;
        list.setItemChecked(position, true);
        listener.onContactListItemSelected(id);
    }

    @Override
    public void onContactLoadComplete(Cursor cursor) {

        ViewHolder holder = getViewHolder();
        if (holder == null) {
            return;
        }

        ContactAdapter adapter = (ContactAdapter) holder.list.getAdapter();
        adapter.swapCursor(cursor);

        if (adapter.getCount() > 0) {
            if (notifyOnFirstLoad) {
                listener.onContactListInitialized(adapter.getItemId(selectedItem));
                notifyOnFirstLoad = false;
            }
            configureList(holder.list);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        // Default, do nothing. Intent sent to search activity (in this case contact detail) with
        // the text entered in the search box in the extra SearchManager.QUERY
        // return false;

        // Return true if plan to intercept this if you want to capture where the use "hits" enter in the search
        // box. Either issue the "search" manually, or suppress it.

        // In this case suppress because only interested in the "suggestions" for the user
        // to choose.
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        // Default, do nothing, if you want the default "search" dropdown to present suggestions
        // based on the content provider.
        return false;

        // Alternatively, the code plans to "re-issue" the list query with a "filter" to reduce the
        // contents of the list fragment. Essentially instead of a drop down over the top of the list,
        // automatically filtering the list contents for selection.

        // Note: this works great on phones, but on tablets, with the detail frame also showing, it may be
        // better to use alternate techniques than search to "ease" the user finding data, or spend time
        // maintaining the "search" box even as the detail fragment is updated.

//       ContactLoaderCallbacks.restartLoader(getActivity(), getLoaderManager(), this, ContactAdapter.PROJECTION, s);
//       return true;
    }

    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /* package */ static class ViewHolder {
        final ListView list;

        ViewHolder(View view) {
            list = (ListView) view.findViewById(R.id.list);
            list.setEmptyView(view.findViewById(R.id.empty));
        }
    }
}
