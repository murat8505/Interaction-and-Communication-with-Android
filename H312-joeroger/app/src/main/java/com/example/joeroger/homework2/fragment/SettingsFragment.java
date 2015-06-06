package com.example.joeroger.homework2.fragment;


import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.example.joeroger.homework2.R;
import com.example.joeroger.homework2.utils.PreferenceUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        String refreshFrequency = PreferenceUtils.getPreference(getActivity(), PreferenceUtils.REFRESH_FREQUENCY_KEY);
        findPreference(PreferenceUtils.REFRESH_FREQUENCY_KEY).setSummary(getRefreshFrequencyTitle(refreshFrequency));
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PreferenceUtils.REFRESH_FREQUENCY_KEY.equals(key)) {
            findPreference(key).setSummary(getRefreshFrequencyTitle(sharedPreferences.getString(key, "")));
        }
    }

    public String getRefreshFrequencyTitle(String newValue) {
        String[] values = getResources().getStringArray(R.array.pref_refresh_frequency_values);
        int i = 0;
        for (String value : values) {
            if (value.equals(newValue)) {
                return getResources().getStringArray(R.array.pref_refresh_frequency_titles)[i];
            }
            ++i;
        }
        return newValue;
    }
}
