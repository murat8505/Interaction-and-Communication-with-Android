package com.example.joeroger.samplebackgrounding.service;

/**
 * Created by jrogers on 5/6/15.
 */
public interface LocationServiceMgr {
    void registerListener(LocationService.LocationServiceListener listener);
    void unregisterListener(LocationService.LocationServiceListener listener);

    void onPlayServicesAreAvailable();
}

