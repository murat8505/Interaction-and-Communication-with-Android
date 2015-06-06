package com.example.joeroger.samplemaps.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

/**
 * Handy utilities for location based operations.
 */
public final class LocationUtils {

    private static final String TAG = "LocationUtils";
    private static final String LOCATION_PREFS = "locationPrefs";
    private static final String LAST_LOCATION_LATITUDE = "lastLocationLatitudePref";
    private static final String LAST_LOCATION_LONGITUDE = "lastLocationLongitudePref";
    private static final String LAST_LOCATION_PROVIDER = "lastLocationProviderPref";
    private static final String LAST_LOCATION_TIME = "lastLocationTimePref";
    private static final String LAST_LOCATION_ACCURACY = "lastLocationAccuracyPref";

    private static final long THIRTY_MINUTES = 30 * 60 * 1000;
    private static final long TWO_HOURS = 2 * 60 * 60 * 1000;
    private static final float ONE_KILOMETER = 1000f;
    private static final float FIVE_HUNDRED_METERS = 500f;
    private static final float TWO_HUNDRED_METERS = 200f;
    private static final float EARTH_RADIUS_KM = 6371;

    private static final LatLng SEATTLE_SW = new LatLng(47.48172, -122.459696);
    private static final LatLng SEATTLE_NE = new LatLng(47.734145, -122.224433);
    public static final LatLng SEATTLE_LATLNG = new LatLng(47.6097, -122.3331);
    public static final LatLngBounds SEATTLE_BOUNDS = new LatLngBounds(SEATTLE_SW, SEATTLE_NE);


    @Nullable
    public static Location getLastLocation(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LOCATION_PREFS, Context.MODE_PRIVATE);

        String provider = sharedPreferences.getString(LAST_LOCATION_PROVIDER, null);
        if (provider == null) {
            return null;
        }

        Location location = new Location(provider);
        location.setLatitude(Double.longBitsToDouble(sharedPreferences.getLong(LAST_LOCATION_LATITUDE, 0)));
        location.setLongitude(Double.longBitsToDouble(sharedPreferences.getLong(LAST_LOCATION_LONGITUDE, 0)));
        location.setAccuracy(sharedPreferences.getFloat(LAST_LOCATION_ACCURACY, 0));
        location.setTime(sharedPreferences.getLong(LAST_LOCATION_TIME, 0));

        return location;
    }

    public static void saveLastLocation(Context context,
                                        Location location) {
        if (location == null) {
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(LOCATION_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(LAST_LOCATION_PROVIDER, location.getProvider());
        editor.putLong(LAST_LOCATION_LATITUDE, Double.doubleToLongBits(location.getLatitude()));
        editor.putLong(LAST_LOCATION_LONGITUDE, Double.doubleToLongBits(location.getLongitude()));
        editor.putLong(LAST_LOCATION_TIME, location.getTime());
        editor.putFloat(LAST_LOCATION_ACCURACY, location.getAccuracy());
        editor.apply();
    }

    /**
     * Determine if the location is acceptable to be displayed in the UI. Try to set up your absolute
     * minimum tolerance here for the app. In this case the app is willing to use a location that is
     * two hours old, provided is accurate to within five hundred meters.
     */
    public static boolean isLocationAcceptable(Location location) {
        if (location == null) return false;

        boolean isTimeAcceptable = System.currentTimeMillis() - location.getTime() <= TWO_HOURS;
        boolean isAccuracyAcceptable = location.getAccuracy() < FIVE_HUNDRED_METERS;

        return isTimeAcceptable && isAccuracyAcceptable;
    }

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {

        if (location == null) {
            Log.d(TAG, "New location is null");
            return false;
        }

        if (currentBestLocation == null) {
            // A new location is always better than no location
            Log.d(TAG, "Using location, no previous location");
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > THIRTY_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -THIRTY_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than thirty minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
        }
        // If the new location is more than thirty minutes older, it must be worse
        else if (isSignificantlyOlder) {
            Log.d(TAG, "Rejecting as significantly older");
            Log.d(TAG, "new location time: " + location.getTime());
            Log.d(TAG, "old location time: " + currentBestLocation.getTime());
            return false;
        }

        float distance = location.distanceTo(currentBestLocation);
        boolean isSignificantlyMoved = distance > ONE_KILOMETER;

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > TWO_HUNDRED_METERS;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            Log.d(TAG, "New location more accurate");
            return true;
        }
        else if (isNewer && !isLessAccurate) {
            Log.d(TAG, "New location is newer and not less accurate");
            return true;
        }
        else if (isNewer && isSignificantlyMoved) {
            Log.d(TAG, "New location is newer and significantly moved");
            return true;
        }
        else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            Log.d(TAG, "New location is newer from same provider, but not significantly less accurate");
            return true;
        }

        Log.d(TAG, "New location is not better");
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /**
     * Find a location x distance at bearing y from the start location.
     *
     * @param start              The starting location
     * @param kilometersDistance distance of the computed location from start point
     * @param bearing            direction the computed location is in relative to start point
     * @return Location or start if computations failed.
     */
    public static Location computeLocation(@NonNull Location start, float kilometersDistance, float bearing) {
        LatLng startLatLng = new LatLng(start.getLatitude(), start.getLongitude());
        // Using Google MapUtils's computation as it already exists, but uses latlng and meters
        LatLng result = SphericalUtil.computeOffset(startLatLng, kilometersDistance * 1000, bearing);
        Location location = new Location(start.getProvider());
        location.setLatitude(result.latitude);
        location.setLongitude(result.longitude);
        return location;
    }
}
