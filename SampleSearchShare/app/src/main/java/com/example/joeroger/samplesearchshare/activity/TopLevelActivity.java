package com.example.joeroger.samplesearchshare.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.joeroger.samplesearchshare.R;
import com.example.joeroger.samplesearchshare.adapter.DrawerAdapter;
import com.example.joeroger.samplesearchshare.fragment.AnimationFragment;
import com.example.joeroger.samplesearchshare.fragment.ContactsFragment;
import com.example.joeroger.samplesearchshare.fragment.DrawableFragment;
import com.example.joeroger.samplesearchshare.fragment.GridViewFragment;
import com.example.joeroger.samplesearchshare.fragment.ReceiveShareFragment;
import com.example.joeroger.samplesearchshare.utils.NotificationHelper;

import org.w3c.dom.Text;

public class TopLevelActivity extends ActionBarActivity
        implements AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String ACTION_NOTIFICATION = "Notification";
    private static final String STATE_SELECTED_POSITION = "selectedPosition";

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private FrameLayout drawerContent;

    private String[] titleArray;
    private int selectedPosition = 1;

    public static PendingIntent buildWidgetPendingIntent(Context context) {
        Intent intent = new Intent(context, TopLevelActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent buildNotificationIntent(Context context) {
        Intent intent = new Intent(context, TopLevelActivity.class);
        intent.setAction(ACTION_NOTIFICATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_level);

        titleArray = getResources().getStringArray(R.array.drawerItems);

        Toolbar toolbar = (Toolbar) findViewById(R.id.topLevelToolBar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            selectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION, selectedPosition);
        }
        else if (ACTION_NOTIFICATION.equals(getIntent().getAction())) {
            selectedPosition = 3;
            NotificationHelper.clearNotification(this);
        }
        else if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            selectedPosition = 5;
        }

        setupDrawer(toolbar);
        setupDrawerContent();

        if (savedInstanceState == null) {
            updateContentView();
        }
        else {
            getSupportActionBar().setTitle(titleArray[selectedPosition-1]);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, selectedPosition);
    }

    @Override
    protected void onDestroy() {
        drawerContent = null;
        drawerToggle = null;
        drawerLayout = null;
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        drawerLayout.closeDrawer(drawerContent);

        if (selectedPosition != position) {
            selectedPosition = position;
            updateContentView();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.settingsView) {
            drawerLayout.closeDrawer(drawerContent);
            startActivity(SettingsActivity.buildIntent(this));
        }
    }

    private void setupDrawer(Toolbar toolbar) {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerLayout.setStatusBarBackground(getPrimaryColorDarkResId());

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // Enable Nav Button (menu) to be shown
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.setDrawerListener(drawerToggle);
    }

    /**
     * helper to pull the color resource id from the theme
     *
     * @return resource id from theme
     */
    private int getPrimaryColorDarkResId() {
        int attributes[] = new int[]{R.attr.colorPrimaryDark};
        TypedArray attrs = getTheme().obtainStyledAttributes(attributes);
        int primaryColorResId = attrs.getResourceId(0, R.color.primary_dark_material_light);
        attrs.recycle();

        return primaryColorResId;
    }

    private void setupDrawerContent() {
        drawerContent = (FrameLayout) findViewById(R.id.drawerContent);

        // Only adjust status bar on lollipop or newer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusBarHeight = getStatusBarHeight();
            addStatusBarOverlay(drawerContent, statusBarHeight);

            // The drawer content by default will not draw under the status bar, the app needs to offset the top
            // of the list so it is "under" status bar. To do this we need to adjust the top margin
            // to be negative. Negative margins are not used often, but primarily used when you want
            // a view to encroach on the space of another view.
            DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawerContent.getLayoutParams();
            params.topMargin = -1 * statusBarHeight;
        }

        setupDrawerList();
    }

    private void setupDrawerList() {
        ListView drawerList = (ListView) drawerContent.findViewById(R.id.drawerList);

        RelativeLayout header = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.drawer_header, drawerList, false);
        drawerList.addHeaderView(header, null, false);

        View footer = LayoutInflater.from(this).inflate(R.layout.drawer_footer, drawerList, false);
        drawerList.addFooterView(footer, null, false);

        initializeFooter(footer);

        BaseAdapter adapter = new DrawerAdapter(this);

        drawerList.setAdapter(adapter);
        drawerList.setItemChecked(selectedPosition, true);
        drawerList.setOnItemClickListener(this);
    }

    /**
     * Helper to add an overlay to provide depth to status bar and allow icons to show.
     *
     * @param header          container for the new status overlay
     * @param statusBarHeight height of the status bar in pixels
     */
    private static void addStatusBarOverlay(FrameLayout header, int statusBarHeight) {
        View statusOverlay = new View(header.getContext());
        statusOverlay.setBackgroundResource(R.color.statusOverlay);
        FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(header.getLayoutParams());
        overlayParams.height = statusBarHeight;
        //overlayParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        statusOverlay.setLayoutParams(overlayParams);
        header.addView(statusOverlay);
    }

    /**
     * Retrieve the status bar height. By default this is a dimen that is not public, so
     * have to manually retrieve. Use a default fallback if fails.
     *
     * @return height of status bar
     */
    private int getStatusBarHeight() {
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId == 0) {
            resId = R.dimen.default_status_bar_height;
        }
        return getResources().getDimensionPixelSize(resId);
    }

    private void initializeFooter(View footer) {
        TextView settings = (TextView) footer.findViewById(R.id.settingsView);
        settings.setText(R.string.settings);
        settings.setOnClickListener(this);
    }

    private void updateContentView() {

        Fragment fragment = null;

        // subtracting 1 to account for "header" view in list
        int adjustedPosition = selectedPosition - 1;
        switch (adjustedPosition) {
            case 0:
                fragment = ContactsFragment.newInstance();
                break;
            case 1:
                fragment = GridViewFragment.newInstance();
                break;
            case 2:
                fragment = DrawableFragment.newInstance();
                break;
            case 3:
                fragment = AnimationFragment.newInstance();
                break;
            case 4:
                String text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
                if (TextUtils.isEmpty(text)) {
                    text = getString(R.string.hello_blank_fragment);
                }
                fragment = ReceiveShareFragment.newInstance(text);
                break;
        }
        getSupportActionBar().setTitle(titleArray[adjustedPosition]);

        // Theoretically you will never have a null fragment, but this
        // can help by avoiding a crash if you add an item in list, but
        // forget to add to this method.
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment, "FRAG")
                    .commit();
        }
    }
}
