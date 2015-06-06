package com.example.joeroger.homework2.fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.joeroger.homework2.R;
import com.example.joeroger.homework2.activity.AddCityActivity;
import com.example.joeroger.homework2.adapter.PlaceItem;
import com.example.joeroger.homework2.adapter.PlaceItemAdapter;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class AddCityFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks,
        SearchView.OnQueryTextListener,
        AdapterView.OnItemClickListener {

    public interface AddCityFragmentListener {
        void onAddCity(LatLng cityLocation);
    }

    private static final String TAG = "AddCityFragment";
    private static final String STATE_LAST_SEARCH = "lastSearch";

    private static final LatLng US_NE = new LatLng(71.3875, -66.947778);
    private static final LatLng US_SW = new LatLng(18.910833, -179.15);
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(US_SW, US_NE);

    private PendingResult<AutocompletePredictionBuffer> pendingAutocompleteResult;
    private PendingResult<PlaceBuffer> pendingPlaceResult;
    private AutocompleteCallback autocompleteCallback;
    private PlaceCallback placeCallback;

    private AddCityFragmentListener listener;
    private GoogleApiClient googleApiClient;
    private String lastSearch = "";

    public AddCityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;
        try {
            listener = (AddCityFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName()
                    + " must implement AddCityFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            lastSearch = savedInstanceState.getString(STATE_LAST_SEARCH, "");
        }

        autocompleteCallback = new AutocompleteCallback();
        placeCallback = new PlaceCallback();

        googleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .build();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_city, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        holder.listView.setOnItemClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment_add_city, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        // You need to reference the activity that will handle the "search" result. In this case
        // I'm sending the result to a dummy activity with no view.
        ComponentName componentName = new ComponentName(getActivity(), AddCityActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        // Adding this fragment as a listener, (variations on solution).
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_LAST_SEARCH, lastSearch);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDestroy() {
        if (pendingAutocompleteResult != null) pendingAutocompleteResult.cancel();
        pendingAutocompleteResult = null;

        if (pendingPlaceResult != null) pendingPlaceResult.cancel();
        pendingPlaceResult = null;

        autocompleteCallback = null;
        placeCallback = null;
        googleApiClient = null;
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Connected to Google Play services!
        // The good stuff goes here, if you are able to do things that are outside of UI
        // interaction. For places, it depends on search so this app has nothing to do.
        Log.d(TAG, "Connected");

        // Perform an initial query, using last user search or default
        String query = lastSearch.length() > 0 ? lastSearch : "aberd";
        lastSearch = query;

        pendingAutocompleteResult =
                Places.GeoDataApi.getAutocompletePredictions(googleApiClient, query,
                        LAT_LNG_BOUNDS, null);

        pendingAutocompleteResult.setResultCallback(autocompleteCallback, 60, TimeUnit.SECONDS);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // Lost connection.
        // Good place to cleanup or suspect any background ops that depend on the connection.
        // Not sure what more you could do...
        // BTW the googleApiClient has isConnected()/isConnecting() methods so no need to track.
        Log.d(TAG, "Connection suspended");
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // return true to intercept user's query to prevent calling search activity
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (pendingAutocompleteResult != null) {
            pendingAutocompleteResult.cancel();
            pendingAutocompleteResult = null;
        }

        // If not connected, no sense in continuing
        if (!googleApiClient.isConnected()) return true;

        // Once city is primed, keep using last results until user types at least 1 different character
        if (query.length() < 1 || lastSearch.startsWith(query)) return true;

        Log.d(TAG, "Searching for: " + query);
        lastSearch = query;

        pendingAutocompleteResult =
                Places.GeoDataApi.getAutocompletePredictions(googleApiClient, query,
                        LAT_LNG_BOUNDS, null);

        pendingAutocompleteResult.setResultCallback(autocompleteCallback, 60, TimeUnit.SECONDS);
        return true;
    }


    public void processAutocompleteResult(AutocompletePredictionBuffer autocompletePredictions) {
        pendingAutocompleteResult = null;
        Status status = autocompletePredictions.getStatus();
        if (!status.isSuccess()) {
            Toast.makeText(getActivity(), "Error contacting API: " + status.getStatusCode(),
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error getting autocomplete prediction API call: " + status.toString());
            autocompletePredictions.release();
            return;
        }

        ViewHolder holder = getViewHolder();
        if (holder == null) {
            autocompletePredictions.release();
            return;
        }

        ArrayList<PlaceItem> items = PlaceItem.buildPlaceItemList(autocompletePredictions);
        autocompletePredictions.release();

        PlaceItemAdapter adapter = (PlaceItemAdapter) holder.listView.getAdapter();
        if (adapter != null) {
            adapter.updateList(items);
        }
        else {
            adapter = new PlaceItemAdapter(holder.listView.getContext(), items);
            holder.listView.setAdapter(adapter);
        }
    }

    public void processPlaceResult(PlaceBuffer places) {
        pendingPlaceResult = null;
        Status status = places.getStatus();
        if (!status.isSuccess()) {
            Toast.makeText(getActivity(), "Error contacting API: " + status.getStatusCode(),
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error getting autocomplete prediction API call: " + status.toString());
            places.release();
            return;
        }

        try {
            int count = places.getCount();
            if (count > 0) {
                Place place = places.get(0);
                listener.onAddCity(place.getLatLng());
            }
        }
        finally {
            places.release();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (pendingPlaceResult != null) {
            pendingPlaceResult.cancel();
            pendingPlaceResult = null;
        }

        PlaceItem item = (PlaceItem) parent.getItemAtPosition(position);
        pendingPlaceResult = Places.GeoDataApi.getPlaceById(googleApiClient, item.getPlaceId());
        pendingPlaceResult.setResultCallback(placeCallback, 10, TimeUnit.SECONDS);
    }

    @Nullable
    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /* package */ static class ViewHolder {
        final ListView listView;

        ViewHolder(View view) {
            listView = (ListView) view.findViewById(R.id.list);
            listView.setEmptyView(view.findViewById(R.id.empty));
        }
    }

    /* package */ class AutocompleteCallback implements ResultCallback<AutocompletePredictionBuffer> {
        @Override
        public void onResult(AutocompletePredictionBuffer autocompletePredictions) {
            processAutocompleteResult(autocompletePredictions);
        }
    }

    /* package */ class PlaceCallback implements ResultCallback<PlaceBuffer> {
        @Override
        public void onResult(PlaceBuffer places) {
            processPlaceResult(places);
        }
    }
}
