package com.example.joeroger.samplesensors.fragment;


import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.joeroger.samplesensors.R;
import com.example.joeroger.samplesensors.adapter.SensorAdapter;

public class SensorListFragment extends Fragment
implements AdapterView.OnItemClickListener {

    private SensorListFragmentListener listener;

    public interface SensorListFragmentListener {
        void onSensorSelected(int sensorType);
    }

    public SensorListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;
        try {
            listener = (SensorListFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName()
                    + " must implement SensorListFragmentListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sensor_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        ListView list = (ListView) view.findViewById(R.id.list);
        list.setEmptyView(view.findViewById(R.id.empty));
        list.setAdapter(new SensorAdapter(view.getContext(), sensorManager.getSensorList(Sensor.TYPE_ALL)));
        list.setOnItemClickListener(this);
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Sensor sensor = (Sensor) parent.getItemAtPosition(position);
        listener.onSensorSelected(sensor.getType());
    }
}
