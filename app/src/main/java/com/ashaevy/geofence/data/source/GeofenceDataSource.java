package com.ashaevy.geofence.data.source;

import com.ashaevy.geofence.data.GeofenceData;

/**
 * Interface for Data Source that stores all geofence data.
 */
public interface GeofenceDataSource {
    void saveGeofenceData(GeofenceData geofenceData);
    GeofenceData readGeofenceData();

    boolean geofenceAdded();
    void saveGeofenceAdded(boolean geofencesAdded);

    void saveGeofenceTransition(int transition);
    int readGeofenceTransition();
}
