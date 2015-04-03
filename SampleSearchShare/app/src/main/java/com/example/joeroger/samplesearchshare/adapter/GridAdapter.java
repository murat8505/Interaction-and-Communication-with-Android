package com.example.joeroger.samplesearchshare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.joeroger.samplesearchshare.R;

import java.util.List;

/**
 * Custom adapter to manage separator in grid view
 */
public class GridAdapter extends ArrayAdapter<String> {

    // Per docs, first view type must start at 0
    private static final int DEFAULT_VIEW_TYPE = 0;
    private static final int NO_SEPARATOR_VIEW_TYPE = 1;
    private final int numColumns;

    public GridAdapter(Context context, List<String> objects) {
        super(context, 0, objects);
        numColumns = context.getResources().getInteger(R.integer.numColumns);
    }

    @Override
    public int getViewTypeCount() {
        // Two types of views if only 1 column
        return numColumns == 1 ? 2 : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (numColumns != 1) return DEFAULT_VIEW_TYPE;

        return position < getCount() - 1 ? DEFAULT_VIEW_TYPE : NO_SEPARATOR_VIEW_TYPE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid, parent, false);
            // Using a view holder as adapter views are recycled. Only need to create a view if the
            // convert view is null
            view.setTag(new ViewHolder(view));
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.title.setText(getItem(position));

        // Hide separator, if this is a NO_SEPARATOR_VIEW_TYPE.
        // Note: In most cases, you would get the item view type prior to inflation as it
        // usually means an alternate layout. In this case only one view will be missing the divider
        // so no real point in creating a separate layout for one case.
        //
        // More often you might have a special separator view that appears in the list between
        // certain items, perhaps a section header, etc
        //
        // Another way to do this, would be to use a listView on phone and gridView on tablet. Then
        // you would have alternate layouts for the fragment and the grid_item. The main advantage
        // is separator will look "normal" for a list.
        //
        if (getItemViewType(position) == NO_SEPARATOR_VIEW_TYPE) {
            // View should be there if we determined only one column should exist
            holder.divider.setVisibility(View.GONE);
        }
        return view;
    }

    /* package */ static class ViewHolder {
        final TextView title;
        final View divider;

        ViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.title);
            divider = view.findViewById(R.id.divider);
        }
    }
}
