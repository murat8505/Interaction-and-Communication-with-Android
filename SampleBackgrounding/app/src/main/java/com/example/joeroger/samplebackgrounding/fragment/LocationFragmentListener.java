package com.example.joeroger.samplebackgrounding.fragment;

import android.location.Location;

/**
 * Shared fragment interface for location samples
 */
public interface LocationFragmentListener {

    public void onNewLocation(Location location);

    public void onPlayServicesUnavailable();
}
