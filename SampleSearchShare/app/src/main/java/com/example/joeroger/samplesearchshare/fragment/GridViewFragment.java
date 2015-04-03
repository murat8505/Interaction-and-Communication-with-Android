package com.example.joeroger.samplesearchshare.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.example.joeroger.samplesearchshare.R;
import com.example.joeroger.samplesearchshare.adapter.GridAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class GridViewFragment extends Fragment {

    // Providing sample data for the grid
    private static final ArrayList<String> data = new ArrayList<>(50);

    static {
        for (int i = 0; i < 50; ++i) {
            data.add("Grid Item " + (i + 1));
        }
    }

    public static GridViewFragment newInstance() {
        return new GridViewFragment();
    }

    public GridViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grid_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridView gridView = (GridView) view.findViewById(R.id.grid);
        gridView.setEmptyView(view.findViewById(R.id.empty));

        // Using a custom adapter to control the separator when only one column
        GridAdapter adapter = new GridAdapter(getActivity(), data);
        gridView.setAdapter(adapter);
    }
}
