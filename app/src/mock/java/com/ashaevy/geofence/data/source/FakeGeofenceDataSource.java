package com.ashaevy.geofence.data.source;

import com.ashaevy.geofence.data.GeofenceData;
import com.google.android.gms.location.Geofence;

/**
 * Fake DataSource. Stores data in memory.
 */

public class FakeGeofenceDataSource implements GeofenceDataSource {

    private static FakeGeofenceDataSource instance = new FakeGeofenceDataSource();

    private GeofenceData mGeofenceData = SPGeofenceDataSource.generateDefaultGeofence();
    private boolean mGeofenceAdded;
    private int mTransition = Geofence.GEOFENCE_TRANSITION_EXIT;

    @Override
    public void saveGeofenceData(GeofenceData geofenceData) {
        mGeofenceData = geofenceData;
    }

    @Override
    public GeofenceData readGeofenceData() {
        return mGeofenceData;
    }

    @Override
    public boolean geofenceAdded() {
        return mGeofenceAdded;
    }

    @Override
    public void saveGeofenceAdded(boolean geofencesAdded) {
        mGeofenceAdded = geofencesAdded;
    }

    @Override
    public void saveGeofenceTransition(int transition) {
        mTransition = transition;
    }

    @Override
    public int readGeofenceTransition() {
        return mTransition;
    }

    public static GeofenceDataSource getInstance() {
        return instance;
    }
}
