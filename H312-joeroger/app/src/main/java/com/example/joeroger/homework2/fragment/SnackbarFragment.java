package com.example.joeroger.homework2.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.joeroger.homework2.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SnackbarFragment extends Fragment
        implements Handler.Callback,
        View.OnClickListener {

    private static final long DELAY = 5 * 1000;
    private static final int SHOW_SNACKBAR = 1;
    private static final int HIDE_SNACKBAR = 2;

    private long animationTime;
    private SnackbarFragmentListener listener;
    private Handler handler;

    public interface SnackbarFragmentListener {
        void onUndoSelected();
    }

    public SnackbarFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        android.support.v4.app.Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;
        try {
            listener = (SnackbarFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName()
                    + " must implement SnackbarFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        animationTime = getActivity().getResources().getInteger(android.R.integer.config_shortAnimTime);
        handler = new Handler(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_snackbar, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        holder.undoButton.setOnClickListener(this);
        view.setVisibility(View.GONE);
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        handler.removeMessages(HIDE_SNACKBAR);
        handler.removeMessages(SHOW_SNACKBAR);
        handler = null;
        super.onDestroy();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case HIDE_SNACKBAR:
                processHideSnackBar();
                return true;
            case SHOW_SNACKBAR:
                handler.removeMessages(HIDE_SNACKBAR);
                View view = getView();
                long delay = 0;
                if (view != null && view.getVisibility() == View.VISIBLE) {
                    processHideSnackBar();
                    delay = animationTime;
                }
                processShowSnackBar(msg.arg1, msg.arg2 == 1, delay);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.undo_button) {
            listener.onUndoSelected();
        }
    }

    public void showSnackBar(@StringRes int msgResId, boolean allowUndo) {
        handler.sendMessage(handler.obtainMessage(SHOW_SNACKBAR, msgResId, allowUndo ? 1 : 0));
    }

    public void hideSnackBar() {
        handler.removeMessages(HIDE_SNACKBAR);
        handler.sendMessage(handler.obtainMessage(HIDE_SNACKBAR));
    }

    private void processShowSnackBar(@StringRes int msgResId, boolean allowUndo, long delay) {

        View view = getView();
        if (view == null) return;

        ViewHolder holder = getViewHolder();
        if (holder == null) return;

        holder.message.setText(msgResId);
        holder.undoButton.setVisibility(allowUndo ? View.VISIBLE : View.GONE);
        int height = view.getMeasuredHeight();
        if (height == 0) {
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            height = view.getMeasuredHeight();
            if (height == 0) {
                height = view.getResources().getDimensionPixelSize(R.dimen.snackbar_height);
            }
        }

        ViewCompat.setTranslationY(view, height);
        view.setVisibility(View.VISIBLE);
        ViewCompat.animate(view)
                .translationY(0)
                .setDuration(animationTime)
                .setStartDelay(delay)
                .start();

        handler.sendMessageDelayed(handler.obtainMessage(HIDE_SNACKBAR), DELAY);
    }

    private void processHideSnackBar() {
        final View view = getView();
        if (view == null) return;

        int height = view.getMeasuredHeight();
        if (height == 0) {
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            height = view.getMeasuredHeight();
            if (height == 0) {
                height = view.getResources().getDimensionPixelSize(R.dimen.snackbar_height);
            }
        }

        ViewCompat.animate(view)
                .translationY(height)
                .setDuration(animationTime)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        view.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    @Nullable
    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /* package */ class ViewHolder {
        final TextView message;
        final View undoButton;

        ViewHolder(View view) {
            message = (TextView) view.findViewById(R.id.message);
            undoButton = view.findViewById(R.id.undo_button);
        }
    }
}
