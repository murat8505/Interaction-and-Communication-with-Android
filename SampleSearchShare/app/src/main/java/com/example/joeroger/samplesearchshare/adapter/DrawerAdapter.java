package com.example.joeroger.samplesearchshare.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.joeroger.samplesearchshare.R;

/**
 * Adapter to handle multiple icons and text
 */
public class DrawerAdapter extends BaseAdapter {

    private final String[] textData;
    private final int[] drawableResData;

    public DrawerAdapter(Context context) {
        super();

        textData = context.getResources().getStringArray(R.array.drawerItems);
        TypedArray images = context.getResources().obtainTypedArray(R.array.drawerImages);
        int size = images.length();
        drawableResData = new int[size];
        for (int i = 0; i < size; ++i) {
            drawableResData[i] = images.getResourceId(i, 0);
        }
        images.recycle();

        if (size != textData.length) {
            throw new IllegalStateException("Text/Images do not match");
        }
    }

    @Override
    public int getCount() {
        return textData.length;
    }

    @Override
    public Object getItem(int position) {
        return textData[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) convertView;
        if (view == null) {
            view = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_list_item, parent, false);
        }

        view.setText(textData[position]);
        view.setCompoundDrawablesWithIntrinsicBounds(drawableResData[position], 0, 0, 0);
        return view;
    }
}
