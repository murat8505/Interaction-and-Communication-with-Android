package com.example.joeroger.homework2.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.joeroger.homework2.R;
import com.example.joeroger.homework2.provider.CityConditionsContract;
import com.example.joeroger.homework2.receiver.FavoriteCityReceiver;
import com.google.android.gms.maps.model.LatLng;


public class FavoriteCityService extends IntentService {
    public static final String ACTION_ADD = "com.example.joeroger.homework2.service.action.ADD";
    public static final String ACTION_TOGGLE = "com.example.joeroger.homework2.service.action.TOGGLE";

    public static final String EXTRA_LAT_LNG = "com.example.joeroger.homework2.service.extra.LATLNG";
    public static final String EXTRA_CITY_ID = "com.example.joeroger.homework2.service.extra.cityId";
    public static final String EXTRA_IS_FAVORITE = "com.example.joeroger.homework2.service.extra.isFavorite";

    private static final String TAG = "FavoriteCityService";

    public static void startAddFavoriteService(Context context, LatLng cityPosition) {
        Intent intent = new Intent(context, FavoriteCityService.class);
        intent.setAction(ACTION_ADD);
        intent.putExtra(EXTRA_LAT_LNG, cityPosition);
        context.startService(intent);
    }

    public static void startToggleFavoriteService(Context context, long cityId, boolean isFavorite) {
        Intent intent = new Intent(context, FavoriteCityService.class);
        intent.setAction(ACTION_TOGGLE);
        intent.putExtra(EXTRA_CITY_ID, cityId);
        intent.putExtra(EXTRA_IS_FAVORITE, isFavorite);
        context.startService(intent);
    }

    public FavoriteCityService() {
        super("FavoriteCityService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Processing work");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ADD.equals(action)) {
                final LatLng latLng = intent.getParcelableExtra(EXTRA_LAT_LNG);
                handleActionAdd(latLng);
            }
            else if (ACTION_TOGGLE.equals(action)) {
                final long cityId = intent.getLongExtra(EXTRA_CITY_ID, CityConditionsContract.NO_CITY_CONDITIONS_ID);
                if (cityId == CityConditionsContract.NO_CITY_CONDITIONS_ID) return;
                final boolean isFavorite = intent.getBooleanExtra(EXTRA_IS_FAVORITE, false);
                handleActionToggle(cityId, isFavorite);
            }
        }
    }

    /**
     * Handle action add in the provided background thread with the provided
     * parameters.
     */
    private void handleActionAdd(LatLng latLng) {
        Log.d(TAG, "Adding favorite...");
        ServiceHelper helper = new ServiceHelper(this);
        boolean success = helper.fetchNewFavorite(latLng);

        if (success) {
            Log.d(TAG, "City added");
            FavoriteCityReceiver.sendAddCityStatus(this, R.string.city_favorited_msg);
        }
        else {
            Log.w(TAG, "Failed to add new city");
            FavoriteCityReceiver.sendAddCityStatus(this, R.string.city_favorited_error_msg);
        }
    }

    private void handleActionToggle(long cityId, boolean isFavorite) {
        ContentValues values = new ContentValues(1);
        values.put(CityConditionsContract.Columns.FAVORITE, isFavorite ? 0 : 1);
        String[] selectionArgs = new String[]{Long.toString(cityId)};
        getContentResolver().update(CityConditionsContract.URI, values,
                CityConditionsContract.WHERE_CITY_ID_MATCHES, selectionArgs);

        final int resId = isFavorite ? R.string.city_unfavorited_msg : R.string.city_favorited_msg;
        FavoriteCityReceiver.sendCityStatus(this, resId, isFavorite);
    }
}
