package com.example.joeroger.homework2.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.joeroger.homework2.R;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseLicenseFragment extends Fragment {


    protected BaseLicenseFragment() {
        // Required empty public constructor
    }


    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_base_license, container, false);
    }

    @Override
    public final void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView text = (TextView) view.findViewById(R.id.license_text);
        text.setText(getLicenseText());
    }

    abstract CharSequence getLicenseText();
}
