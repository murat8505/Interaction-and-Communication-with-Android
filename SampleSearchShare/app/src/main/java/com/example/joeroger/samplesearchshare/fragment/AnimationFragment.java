package com.example.joeroger.samplesearchshare.fragment;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.example.joeroger.samplesearchshare.R;
import com.example.joeroger.samplesearchshare.view.RingView;

/**
 * A simple {@link Fragment} subclass.
 */
public class AnimationFragment extends Fragment
        implements View.OnClickListener, View.OnTouchListener {

    private int distance100dp;
    private int timeLong;
    private float startX;
    private float startY;

    public static AnimationFragment newInstance() {
        return new AnimationFragment();
    }

    public AnimationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        distance100dp = activity.getResources().getDimensionPixelSize(R.dimen.animation_100dp);
        timeLong = activity.getResources().getInteger(android.R.integer.config_longAnimTime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_animation, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        holder.ringView.setOnTouchListener(this);
        holder.viewAnimationButton.setOnClickListener(this);
        holder.propAnimationButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.view_animation_button:
                performViewAnimation(getViewHolder().ringView);
                break;
            case R.id.prop_animation_button:
                performPropAnimation(getViewHolder().ringView);
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Save where we started at
                startX = event.getRawX();
                startY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                // Use difference to compute how far the view should move
                // and move the view
                v.setTranslationX(event.getRawX() - startX);
                v.setTranslationY(event.getRawY() - startY);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // Move view to final spot and animate back to start
                v.setTranslationX(event.getRawX() - startX);
                v.setTranslationY(event.getRawY() - startY);
                ViewCompat.animate(v)
                        .translationY(0)
                        .translationX(0)
                        .setDuration(timeLong)
                        .setInterpolator(AnimationUtils.loadInterpolator(getActivity(), android.R.interpolator.accelerate_decelerate))
                        .withLayer();
                break;
        }
        return true;
    }

    private void performViewAnimation(final View view) {

        final int height = view.getHeight();

        // Demonstrating using end actions. However, an alternate way is to rig start delays, but that
        // is a little trickier if the animations get out of sync.

        /* building animations in reverse */
        final Runnable restoreSquish = new Runnable() {
            @Override
            public void run() {
                ViewCompat.animate(view)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .translationY(0)
                        .withLayer()
                        .setDuration(timeLong / 2)
                        .setInterpolator(AnimationUtils.loadInterpolator(getActivity(), android.R.interpolator.anticipate))
                ;//.start();  Start is optional for ViewPropertyAnimator. Otherwise it takes effect on next opportunity
            }
        };

        final Runnable squishView = new Runnable() {
            @Override
            public void run() {
                // reset rotation
                ViewCompat.setRotation(view, 0);

                ViewCompat.animate(view)
                        .scaleX(1.1f)
                        .scaleY(.75f)
                                // reducing height by 75%. Moving Y down 1/8 to keep it on the "ground" which half the scale factor
                        .translationYBy(height / 8)
                        .withLayer()
                        .setInterpolator(AnimationUtils.loadInterpolator(getActivity(), android.R.interpolator.accelerate_decelerate))
                        .setDuration(timeLong / 2)
                        .withEndAction(restoreSquish)
                ;//.start();  Start is optional for ViewPropertyAnimator. Otherwise it takes effect on next opportunity
            }
        };

        final Runnable fallBack = new Runnable() {
            @Override
            public void run() {
                ViewCompat.animate(view)
                        // Using straight values here to move back to original position of 0,0
                        .translationX(0)
                        .translationY(0)
                                // Easier is to use rotationBy(360). But demonstrating resetting rotation. See squish
                        .rotation(360)
                        .withLayer()
                        .setDuration(2 * timeLong)
                        .setInterpolator(AnimationUtils.loadInterpolator(getActivity(), android.R.interpolator.anticipate))
                        .withEndAction(squishView)
                ;//.start();  Start is optional for ViewPropertyAnimator. Otherwise it takes effect on next opportunity
            }
        };

        final Runnable returnToCenter = new Runnable() {
            @Override
            public void run() {
                ViewCompat.animate(view)
                        .translationYBy(-distance100dp)
                        .translationXBy(distance100dp / 2)
                        .withLayer()
                        .setDuration(timeLong)
                        .setInterpolator(AnimationUtils.loadInterpolator(getActivity(), android.R.interpolator.decelerate_cubic))
                        .withEndAction(fallBack)
                ;//.start();  Start is optional for ViewPropertyAnimator. Otherwise it takes effect on next opportunity
            }
        };

        ViewCompat.animate(view)
                // By moves from current position by amount specified.
                .translationXBy(-distance100dp / 2)
                .translationYBy(-distance100dp)
                .withLayer()
                .withEndAction(returnToCenter)
                .setInterpolator(AnimationUtils.loadInterpolator(getActivity(), android.R.interpolator.accelerate_cubic))
                .setDuration(timeLong)
        ;//.start();  Start is optional for ViewPropertyAnimator. Otherwise it takes effect on next opportunity
    }

    private void performPropAnimation(RingView view) {
        // You can actually do this with one animator by giving different values.
        //ObjectAnimator animator = ObjectAnimator.ofInt(view, "progress", 50, 0, 50, 100, 50);

        ObjectAnimator animator1 = ObjectAnimator.ofInt(view, "progress", 50, 0).setDuration(timeLong);
        ObjectAnimator animator2 = ObjectAnimator.ofInt(view, "progress", 0, 100).setDuration(timeLong * 2);
        ObjectAnimator animator3 = ObjectAnimator.ofInt(view, "progress", 100, 50).setDuration(timeLong);
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(animator1, animator2, animator3);
        set.start(); // Start is required for object animation.
    }

    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /* package */ static class ViewHolder {
        final View viewAnimationButton;
        final View propAnimationButton;
        final RingView ringView;

        ViewHolder(View view) {
            viewAnimationButton = view.findViewById(R.id.view_animation_button);
            propAnimationButton = view.findViewById(R.id.prop_animation_button);
            ringView = (RingView) view.findViewById(R.id.ring_view);
        }
    }
}
