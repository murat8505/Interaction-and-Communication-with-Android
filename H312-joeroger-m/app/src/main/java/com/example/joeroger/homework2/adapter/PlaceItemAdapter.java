package com.example.joeroger.homework2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.joeroger.homework2.R;

import java.util.List;

public class PlaceItemAdapter extends ArrayAdapter<PlaceItem> {
    public PlaceItemAdapter(Context context, List<PlaceItem> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_material_simple_one_line_item, parent, false);
        }

        TextView text = (TextView) view.findViewById(R.id.text1);
        text.setText(getItem(position).getDescription());

        return view;
    }

    public void updateList(List<PlaceItem> items) {
        clear();
        addAll(items);
    }
}