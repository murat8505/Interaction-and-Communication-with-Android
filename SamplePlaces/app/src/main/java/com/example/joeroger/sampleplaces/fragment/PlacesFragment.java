package com.example.joeroger.sampleplaces.fragment;


import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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

import com.example.joeroger.sampleplaces.R;
import com.example.joeroger.sampleplaces.activity.SearchActivity;
import com.example.joeroger.sampleplaces.adapter.PlaceItem;
import com.example.joeroger.sampleplaces.adapter.PlaceItemAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlacesFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        PlayServicesDialogFragment.PlayServicesDialogFragmentListener,
        SearchView.OnQueryTextListener,
        ResultCallback<AutocompletePredictionBuffer>,
        AdapterView.OnItemClickListener {

    private static final String TAG = "PlacesFragment";

    private static final String GPS_DIALOG_TAG = "GooglePlayServiceDialog";

    private static final String STATE_RESOLVING_ERROR = "resolvingError";
    private static final int RESOLVE_ERROR_REQUEST = 110;
    private static final LatLng SEATTLE_SW = new LatLng(47.48172, -122.459696);
    private static final LatLng SEATTLE_NE = new LatLng(47.734145, -122.224433);
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(SEATTLE_SW, SEATTLE_NE);

    private PendingResult<AutocompletePredictionBuffer> pendingResult;

    private PlacesFragmentListener listener;
    private GoogleApiClient googleApiClient;
    private AutocompleteFilter filter;
    private boolean resolvingError;

    public interface PlacesFragmentListener {
        public void startImageActivity();

        public void noPlayServicesAvailable();
    }

    public PlacesFragment() {
        // Required empty public constructor
    }

    public void onPlayServicesAreAvailable() {
        // Notified by the activity that play services are now available. If
        // not connected, or connecting, then restart the connection process.
        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;
        try {
            listener = (PlacesFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName()
                    + " must implement DailyForecastFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        resolvingError = savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR);

        googleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        ArrayList<Integer> filterValues = new ArrayList<>();
        filterValues.add(Place.TYPE_ESTABLISHMENT);
        filter = AutocompleteFilter.create(filterValues);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_places, container, false);
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
        inflater.inflate(R.menu.menu_fragment_places, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        // You need to reference the activity that will handle the "search" result. In this case
        // I'm sending the result to a dummy activity with no view.
        ComponentName componentName = new ComponentName(getActivity(), SearchActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        // Adding this fragment as a listener, (variations on solution).
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        // If resolving an error, do not start the client... as it is already started.
        if (!resolvingError) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, resolvingError);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Connected to Google Play services!
        // The good stuff goes here, if you are able to do things that are outside of UI
        // interaction. For places, it depends on search so this app has nothing to do.
        Log.d("JSR", "Connected");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // Lost connection.
        // Good place to cleanup or suspect any background ops that depend on the connection.
        // Not sure what more you could do...
        // BTW the googleApiClient has isConnected()/isConnecting() methods so no need to track.
        Log.d("JSR", "Connection suspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (resolvingError) {
            // Already attempting to resolve an error.
            return;
        }

        if (connectionResult.hasResolution()) {
            try {
                resolvingError = true;
                connectionResult.startResolutionForResult(getActivity(), RESOLVE_ERROR_REQUEST);
            }
            catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                googleApiClient.connect();
                resolvingError = false;
            }
        }
        else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            // Must use your own fragment wrapper around the dialog or rotation does not work correctly.
            // Luckily you could essentially copy the PlayServicesDialogFragment from project to project.
            // Works with both activities/fragments as parent. However, the onActivityResult() is always
            // routed to the "Activity". So the activity is responsible for notifying any fragments that
            // google play services are available.
            DialogFragment fragment = PlayServicesDialogFragment.newInstance(connectionResult.getErrorCode());
            fragment.show(getChildFragmentManager(), GPS_DIALOG_TAG);
            resolvingError = true;
        }
    }

    @Override
    public void onDialogCancelled() {
        Log.d(TAG, "Dialog cancelled");
        listener.noPlayServicesAvailable();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Return true if plan to intercept this if you want to capture where the use "hits" enter in the search
        // box. Either issue the "search" manually, or suppress it.

        // In this case suppress because only interested in the "suggestions" for the user
        // to choose.

        // Note: on emulator using your laptop/desktop keyboard this will get ignored.
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (pendingResult != null) {
            pendingResult.cancel();
            pendingResult = null;
        }

        if (newText.length() < 2 || !googleApiClient.isConnected()) return true;

        Log.d("PlacesFragment", "Searching for: " + newText);

        pendingResult =
                Places.GeoDataApi.getAutocompletePredictions(googleApiClient, newText,
                        LAT_LNG_BOUNDS, filter);

        pendingResult.setResultCallback(this, 60, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public void onResult(AutocompletePredictionBuffer autocompletePredictions) {
        pendingResult = null;
        Status status = autocompletePredictions.getStatus();
        if (!status.isSuccess()) {
            Toast.makeText(getActivity(), "Error contacting API: " + status.toString(),
                    Toast.LENGTH_SHORT).show();
            Log.e("JSR", "Error getting autocomplete prediction API call: " + status.toString());
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

        PlaceItemAdapter adapter = new PlaceItemAdapter(holder.listView.getContext(), items);
        holder.listView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        listener.startImageActivity();
    }

    @Nullable
    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /* package */ static class ViewHolder {
        final ListView listView;
        final View attribution;

        ViewHolder(View view) {
            listView = (ListView) view.findViewById(R.id.list);
            listView.setEmptyView(view.findViewById(R.id.empty));
            attribution = view.findViewById(R.id.attribution);
        }
    }
}
