package com.example.joeroger.samplesensors.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.joeroger.samplesensors.R;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;


public class UserMovementAdapter extends ArrayAdapter<DetectedActivity> {

    public UserMovementAdapter(Context context) {
        super(context, 0, new ArrayList<DetectedActivity>(0));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        DetectedActivity activity = getItem(position);

        TextView textView = (TextView) view.findViewById(android.R.id.text1);

        textView.setText(getContext().getString(R.string.user_movement_item,
                activity.getConfidence(), getTypeString(activity.getType())));

        return view;
    }

    public void updateList(List<DetectedActivity> list) {
        clear();
        addAll(list);
    }

    private String getTypeString(int type) {
        switch (type) {
            case DetectedActivity.IN_VEHICLE:
                return "In Vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On Bicycle";
            case DetectedActivity.ON_FOOT:
                return "On Foot";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.TILTING:
                return "Tilting";
            case DetectedActivity.WALKING:
                return "Walking";
            default:
                return "Unknown";
        }
    }
}
