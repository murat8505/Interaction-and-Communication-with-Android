package com.example.joeroger.samplesensors.fragment;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.joeroger.samplesensors.BuildConfig;
import com.example.joeroger.samplesensors.R;
import com.example.joeroger.samplesensors.adapter.UserMovementAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserMovementFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        PlayServicesDialogFragment.PlayServicesDialogFragmentListener {

    private static final String TAG = "UserMovementFragment";

    private static final String ACTION_NEW_RESULT = BuildConfig.APPLICATION_ID + ".fragment.UserMovementFragment.action.newResult";

    private static final String STATE_RESOLVING_ERROR = "resolvingError";
    private static final String GPS_DIALOG_TAG = "GooglePlayServiceDialog";
    private static final int RESOLVE_ERROR_REQUEST = 110;
    private static final int BROADCAST_REQUEST = 111;

    private static final long INTERVAL = 100; // 100 ms. Should be adjusted to your app needs.

    private UserMovementAdapter adapter;
    private GoogleApiClient googleApiClient;
    private UserMovementFragmentListener listener;
    private UserMovementReceiver receiver;
    private boolean resolvingError = false;

    public interface UserMovementFragmentListener {
        void onPlayServicesUnavailable();
    }

    public UserMovementFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;
        try {
            listener = (UserMovementFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName()
                    + " must implement UserMovementFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resolvingError = savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR);

        googleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_movement, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new UserMovementAdapter(view.getContext());

        ListView list = (ListView) view.findViewById(R.id.list);
        list.setEmptyView(view.findViewById(R.id.empty));
        list.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        googleApiClient.connect();
        receiver = new UserMovementReceiver();
        IntentFilter filter = new IntentFilter(ACTION_NEW_RESULT);
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        if (googleApiClient.isConnected()) {
            Log.d(TAG, "Stopping activity recognition monitoring");
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                    googleApiClient, buildIntent(getActivity()));
            googleApiClient.disconnect();
        }
        getActivity().unregisterReceiver(receiver);

        super.onPause();
    }

    @Override
    public void onDestroyView() {
        adapter = null;
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @Override
    public void onConnected(Bundle bundle) {
        PendingResult<Status> result = ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                googleApiClient,
                INTERVAL,
                buildIntent(getActivity().getApplicationContext()));

        result.setResultCallback(new MyResultCallback());
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Nothing to do...
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
        listener.onPlayServicesUnavailable();
    }

    public void onPlayServicesAreAvailable() {
        resolvingError = false;

        // Notified by the activity that play services are now available. If
        // not connected, or connecting, then restart the connection process.
        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    class MyResultCallback implements ResultCallback<Status> {
        @Override
        public void onResult(Status status) {
            if (status.isSuccess()) {
                Log.d(TAG, "Activity Recognition started");
            }
            else {
                Log.w(TAG, "Activity Recognition failed");
                googleApiClient.disconnect();
            }
        }
    }

    static PendingIntent buildIntent(Context context) {
        Intent intent = new Intent(ACTION_NEW_RESULT);
        return PendingIntent.getBroadcast(context, BROADCAST_REQUEST, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    class UserMovementReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            List<DetectedActivity> list = result.getProbableActivities();
            adapter.updateList(list);
        }
    }
}
