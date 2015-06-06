package com.example.joeroger.homework2.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.joeroger.homework2.R;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class GPSLicenseFragment extends BaseLicenseFragment {


    public static GPSLicenseFragment newInstance() {
        return new GPSLicenseFragment();
    }

    public GPSLicenseFragment() {
        // Required empty public constructor
    }

    @Override
    CharSequence getLicenseText() {
        return GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getActivity());
    }
}
