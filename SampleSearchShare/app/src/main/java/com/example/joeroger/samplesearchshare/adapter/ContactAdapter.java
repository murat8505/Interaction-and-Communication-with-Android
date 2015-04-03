package com.example.joeroger.samplesearchshare.adapter;

import android.content.Context;
import android.support.v4.widget.SimpleCursorAdapter;

import com.example.joeroger.samplesearchshare.R;
import com.example.joeroger.samplesearchshare.provider.ContactContract;


public class ContactAdapter extends SimpleCursorAdapter {

    public static final String[] PROJECTION = new String[] {
            ContactContract.Columns._ID,
            ContactContract.Columns.NAME
    };

    public static final String[] COLUMN_MAP = new String[] {
            ContactContract.Columns.NAME
    };

    public static final int[] ID_MAP = new int[] {
            R.id.text1
    };

    public ContactAdapter(Context context) {
        super(context, R.layout.adapter_simple_list_item, null, COLUMN_MAP, ID_MAP, 0);
    }
}
