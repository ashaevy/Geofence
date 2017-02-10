package com.ashaevy.geofence.transition;

import android.location.Location;
import android.os.Bundle;

import com.ashaevy.geofence.GeofenceContract;
import com.google.android.gms.maps.model.LatLng;

/**
 * Interface that defines contract between Geofence transition client
 * and provider of Geofence transitions.
 */
public interface GeofenceHelper {

    void setPresenter(GeofenceContract.Presenter presenter);

    void create(Bundle savedInstanceState);

    void start();

    void saveInstanceState(Bundle outState);

    void stop();

    void addGeofence(LatLng position, double radius);

    void removeGeofence();

    void setMockLocation(Location mockLocation);

    void enableMockLocation();

    void disableMockLocation();

    void notifyAboutNetworkChange();
}
