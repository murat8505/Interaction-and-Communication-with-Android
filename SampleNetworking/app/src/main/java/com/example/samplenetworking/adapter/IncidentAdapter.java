package com.example.samplenetworking.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.samplenetworking.R;
import com.example.samplenetworking.provider.Seattle911Contract;

import java.util.Date;

/**
 * Created by jrogers on 4/6/15.
 */
public class IncidentAdapter extends CursorAdapter {

    public static final String[] PROJECTION = {
            Seattle911Contract.Columns._ID,
            Seattle911Contract.Columns.DESCRIPTION,
            Seattle911Contract.Columns.LOCATION,
            Seattle911Contract.Columns.CLEARANCE_DATE
    };

    private static final int DESCRIPTION_POS = 1;
    private static final int LOCATION_POS = 2;
    private static final int CLEARANCE_DATE_POS = 3;

    public IncidentAdapter(Context context) {
        super(context, null, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_material_simple_three_line_item, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.text1.setText(cursor.getString(DESCRIPTION_POS));
        holder.text2.setText(cursor.getString(LOCATION_POS));
        Date eventTime = new Date(cursor.getLong(CLEARANCE_DATE_POS));
        String date = DateFormat.getDateFormat(context).format(eventTime);
        String time = DateFormat.getTimeFormat(context).format(eventTime);
        holder.text3.setText(date + " " + time);
    }

    /* package */ class ViewHolder {
        final TextView text1;
        final TextView text2;
        final TextView text3;

        ViewHolder(View view) {
            text1 = (TextView) view.findViewById(R.id.text1);
            text2 = (TextView) view.findViewById(R.id.text2);
            text3 = (TextView) view.findViewById(R.id.text3);
        }
    }
}
