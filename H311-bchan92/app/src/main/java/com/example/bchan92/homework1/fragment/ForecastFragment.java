package com.example.bchan92.homework1.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.bchan92.homework1.R;

/**
 * Displays a five day weather forecast in a list. Consists of the daily image, high/low temperatures
 * and day of the week.
 */

public class ForecastFragment extends Fragment
    implements AdapterView.OnItemClickListener {

    private static final String EXTRA_VALUES = ForecastFragment.class.getSimpleName() + ".values";

    private int high_temp;
    private int low_temp;

    private ForecastFragmentListener listener;

    public interface ForecastFragmentListener {
        public void onDaySelected();
    }

    public ForecastFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (ForecastFragmentListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.getClass().getSimpleName()
                    + " must implement ForecastFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forecast, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // View holder here is not necessarily required since not accessing the
        // list view after this method. But including for example purposes
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // open up the day detail fragment
    }

    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /* package */ static class ViewHolder {

        ViewHolder(View view) {

        }
    }

}
