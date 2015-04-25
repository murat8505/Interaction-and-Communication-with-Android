package com.example.joeroger.sampleplaces.fragment;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.joeroger.sampleplaces.R;
import com.example.joeroger.sampleplaces.adapter.ImageAdapter;
import com.example.joeroger.sampleplaces.bitmap.CacheUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImageFragment extends Fragment {


    public ImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView list = (ListView) view.findViewById(R.id.list);
        list.setEmptyView(view.findViewById(R.id.empty));

        Context context = list.getContext();
        CacheUtils.initializeCache(context);
        list.setAdapter(new ImageAdapter(context, context.getResources().getStringArray(R.array.imageUrls)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CacheUtils.flushCache();
    }
}
