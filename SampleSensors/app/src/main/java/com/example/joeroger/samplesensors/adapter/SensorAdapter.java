package com.example.joeroger.samplesensors.adapter;

import android.content.Context;
import android.hardware.Sensor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.joeroger.samplesensors.utils.SensorUtils;

import java.util.ArrayList;
import java.util.List;


public class SensorAdapter extends ArrayAdapter<Sensor> {

    public SensorAdapter(Context context, List<Sensor> data) {
        super(context, 0, new ArrayList<>(data));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        Sensor sensor = getItem(position);

        TextView textView = (TextView) view.findViewById(android.R.id.text1);

        textView.setText(SensorUtils.getSensorType(sensor));

        return view;
    }
}
