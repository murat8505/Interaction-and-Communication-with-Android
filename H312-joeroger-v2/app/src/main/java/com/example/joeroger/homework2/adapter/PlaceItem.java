package com.example.joeroger.homework2.adapter;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;

import java.util.ArrayList;

public class PlaceItem {
    final private String description;
    final private String placeId;

    public PlaceItem(AutocompletePrediction prediction) {
        description = prediction.getDescription();
        placeId = prediction.getPlaceId();
    }

    public String getDescription() {
        return description;
    }

    public String getPlaceId() {
        return placeId;
    }

    public static ArrayList<PlaceItem> buildPlaceItemList(AutocompletePredictionBuffer buffer) {
        int count = buffer.getCount();
        ArrayList<PlaceItem> result = new ArrayList<>(count);
        for (int i = 0; i < count; ++i) {
            result.add(new PlaceItem(buffer.get(i)));
        }
        return result;
    }
}
