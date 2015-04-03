package com.example.joeroger.samplesearchshare.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.example.joeroger.samplesearchshare.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
