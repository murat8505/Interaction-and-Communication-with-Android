package com.example.joeroger.homework2.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.joeroger.homework2.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class OpenSourceLicenseFragment extends Fragment
    implements View.OnClickListener {

    private OpenSourceLicenseFragmentListener listener;

    public static OpenSourceLicenseFragment newInstance() {
        return new OpenSourceLicenseFragment();
    }

    public interface OpenSourceLicenseFragmentListener {
        void showLicenseFragment(Fragment licenseFragment);
    }

    public OpenSourceLicenseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        super.onAttach(activity);
        Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;
        try {
            listener = (OpenSourceLicenseFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName()
                    + " must implement penSourceLicenseFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_open_source_license, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.apache_license_button).setOnClickListener(this);
        view.findViewById(R.id.gps_license_button).setOnClickListener(this);
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.apache_license_button:
                listener.showLicenseFragment(ApacheLicenseFragment.newInstance());
                break;
            case R.id.gps_license_button:
                listener.showLicenseFragment(GPSLicenseFragment.newInstance());
                break;
        }
    }
}
