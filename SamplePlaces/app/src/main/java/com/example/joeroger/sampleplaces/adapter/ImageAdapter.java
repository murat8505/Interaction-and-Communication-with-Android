package com.example.joeroger.sampleplaces.adapter;


import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.joeroger.sampleplaces.R;
import com.example.joeroger.sampleplaces.bitmap.BitmapLoadTask;

import org.w3c.dom.Text;

import java.util.List;

public class ImageAdapter extends ArrayAdapter<String> {
    public ImageAdapter(Context context, String[] objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_material_simple_one_line_image_item, parent, false);
            view.setTag(new ViewHolder(view));
        }

        ViewHolder holder = (ViewHolder) view.getTag();

        String url = getItem(position);
        holder.text.setText(url);
        // Load the bitmap asynchronously. An improved version would use a derived ImageView which would
        // cancel the task when the view detects it was destroyed.
        new BitmapLoadTask(url, holder.image).executeOnExecutor(BitmapLoadTask.BITMAP_THREAD_POOL_EXECUTOR);

        return view;
    }

    /* package */ static class ViewHolder {
        final TextView text;
        final ImageView image;

        ViewHolder(View view) {
            text = (TextView) view.findViewById(R.id.text1);
            image = (ImageView) view.findViewById(R.id.image);
        }
    }
}
