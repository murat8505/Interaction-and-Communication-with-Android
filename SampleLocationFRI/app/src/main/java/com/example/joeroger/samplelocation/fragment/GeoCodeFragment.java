package com.example.joeroger.samplelocation.fragment;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.joeroger.samplelocation.BuildConfig;
import com.example.joeroger.samplelocation.R;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GeoCodeFragment extends Fragment {

    private static final String TAG = "GeoCodeFragment";

    private String locationText = "Unknown";

    public GeoCodeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_geo_code, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        if (Geocoder.isPresent()) {
            holder.locationView.setText(locationText);
        }
        else {
            holder.locationView.setVisibility(View.GONE);
        }
    }

    public void setLocation(Location location) {

        if (Geocoder.isPresent()) {
            new GeoCoderTask(this, location).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Nullable
    protected ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /* package */ static class ViewHolder {
        final TextView locationView;

        ViewHolder(View view) {
            locationView = (TextView) view.findViewById(R.id.location);
        }
    }


    /* package */ static class GeoCoderTask extends AsyncTask<Void, Void, String> {

        private final WeakReference<GeoCodeFragment> fragmentRef;
        private final Context applicationContext;
        private final Location location;

        GeoCoderTask(GeoCodeFragment fragment, Location location) {
            fragmentRef = new WeakReference<>(fragment);
            applicationContext = fragment.getActivity().getApplicationContext();
            this.location = location;
        }

        @Override
        protected String doInBackground(Void... params) {

            Geocoder geocoder = new Geocoder(applicationContext);

            try {
                // For simplicity using first match. If you were presenting a chooser, you may show this result in a list
                // so user could pick address. Note: depending on what you care about, you may have similiar info. For example
                // first is probably good enough for city.
                List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                if (!addressList.isEmpty()) {
                    Address address = addressList.get(0);

                    StringBuilder location = new StringBuilder();
                    for (int i = 0; i < address.getMaxAddressLineIndex(); ++i) {
                        if (i > 0) {
                            location.append('\n');
                        }
                        location.append(address.getAddressLine(i));
                    }

                    // Just city/state...
                    // location.append(address.getLocality()).append(", ").append(address.getAdminArea());
                    return location.toString();

                }
            }
            catch (IOException e) {
                Log.w(TAG, "Unexpected exception: " + e.getMessage());
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Stack", e);
                }
            }

            return "Unknown";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            GeoCodeFragment fragment = fragmentRef.get();
            if (fragment == null) return;

            ViewHolder holder = fragment.getViewHolder();
            if (holder != null) {
                holder.locationView.setText(s);
            }
        }
    }
}
