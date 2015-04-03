package com.example.joeroger.samplesearchshare.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.joeroger.samplesearchshare.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddContactFragment extends Fragment
implements View.OnClickListener {

    private AddContactFragmentListener listener;

    public interface AddContactFragmentListener {
        public void onContactAdded(String name);
    }

    public AddContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;
        try {
            listener = (AddContactFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName()
                    + " must implement AddContactFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_contact, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        holder.submitButton.setOnClickListener(this);
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.submit_button) {
            ViewHolder holder = getViewHolder();
            String name = holder.nameView.getText().toString();
            if (TextUtils.isEmpty(name)) {
                holder.nameView.setError(getString(R.string.no_value_error));
                return;
            }
            listener.onContactAdded(name);
        }
    }

    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /* package */ static class ViewHolder {
        final EditText nameView;
        final View submitButton;

        ViewHolder(View view) {
            nameView = (EditText) view.findViewById(R.id.name);
            submitButton = view.findViewById(R.id.submit_button);
        }
    }
}
