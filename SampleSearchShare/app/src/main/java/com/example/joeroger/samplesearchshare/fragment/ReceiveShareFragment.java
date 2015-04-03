package com.example.joeroger.samplesearchshare.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.joeroger.samplesearchshare.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReceiveShareFragment extends Fragment {

    private static final String ARG_TEXT = "text";

    private String text = "";

    public static ReceiveShareFragment newInstance(String text) {
        ReceiveShareFragment fragment = new ReceiveShareFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_TEXT, text);
        fragment.setArguments(arguments);
        return fragment;
    }

    public ReceiveShareFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            text = args.getString(ARG_TEXT, "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_receive_share, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Not using view holder only because never change value after fragment starts.
        TextView textView = (TextView) view.findViewById(R.id.share_text);
        textView.setText(text);
    }
}
