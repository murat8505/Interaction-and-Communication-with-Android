package com.example.joeroger.samplemaps.utils;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.maps.android.heatmaps.WeightedLatLng;

/**
 * Created by jrogers on 5/13/15.
 */
public final class IncidentUtils {

    public static float translateCategoryToColor(String category) {
        switch (category) {
            case "ASSAULTS":
            case "HOMICIDE":
            case "ROBBERY":
            case "THREATS, HARASSMENT":
                return BitmapDescriptorFactory.HUE_RED;
            case "AUTO THEFTS":
            case "BURGLARY":
            case "CAR PROWL":
            case "FRAUD CALLS":
            case "LIQUOR VIOLATIONS":
            case "NARCOTICS COMPLAINTS":
            case "OTHER PROPERTY":
            case "PERSON DOWN/INJURY":
            case "PROPERTY DAMAGE":
            case "PROSTITUTION":
            case "PROWLER":
            case "SHOPLIFTING":
            case "TRESPASS":
            case "WEAPONS CALLS":
                return BitmapDescriptorFactory.HUE_YELLOW;
            default:
                return BitmapDescriptorFactory.HUE_GREEN;
        }
    }

    public static double translateCategoryToIntensity(String category) {
        switch (category) {
            case "ASSAULTS":
            case "HOMICIDE":
            case "ROBBERY":
            case "THREATS, HARASSMENT":
                return WeightedLatLng.DEFAULT_INTENSITY * 8;
            case "AUTO THEFTS":
            case "BURGLARY":
            case "CAR PROWL":
            case "FRAUD CALLS":
            case "LIQUOR VIOLATIONS":
            case "NARCOTICS COMPLAINTS":
            case "OTHER PROPERTY":
            case "PROPERTY DAMAGE":
            case "PROSTITUTION":
            case "PROWLER":
            case "SHOPLIFTING":
            case "TRESPASS":
            case "WEAPONS CALLS":
                return WeightedLatLng.DEFAULT_INTENSITY * 3;
            default:
                return WeightedLatLng.DEFAULT_INTENSITY;
        }
    }

    public static boolean isMajor(String category) {
        switch (category) {
            case "ASSAULTS":
            case "HOMICIDE":
            case "ROBBERY":
            case "THREATS, HARASSMENT":
                return true;
            default:
                return false;
        }
    }
}