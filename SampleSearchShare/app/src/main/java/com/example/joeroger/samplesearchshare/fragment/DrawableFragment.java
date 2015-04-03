package com.example.joeroger.samplesearchshare.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.joeroger.samplesearchshare.R;
import com.example.joeroger.samplesearchshare.utils.NotificationHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class DrawableFragment extends Fragment
        implements View.OnClickListener {


    public static DrawableFragment newInstance() {
        return new DrawableFragment();
    }

    public DrawableFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drawable, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View fabButton = view.findViewById(R.id.fab_button);
        fabButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_button) {
            NotificationHelper.createNotification(getActivity());
        }
    }
}
