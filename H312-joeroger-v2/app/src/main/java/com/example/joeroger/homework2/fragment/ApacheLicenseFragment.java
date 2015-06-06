package com.example.joeroger.homework2.fragment;


import com.example.joeroger.homework2.R;

/**
 * Apache license fragment
 */
public class ApacheLicenseFragment extends BaseLicenseFragment {

    public static ApacheLicenseFragment newInstance() {
        return new ApacheLicenseFragment();
    }

    public ApacheLicenseFragment() {
        // Required empty public constructor
    }

    @Override
    CharSequence getLicenseText() {
        return getText(R.string.apache_license);
    }
}
