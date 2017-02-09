package com.ashaevy.geofence;

import android.location.Location;
import android.os.Bundle;

import com.ashaevy.geofence.data.GeofenceData;

/**
 * Contract between Views and Presenters.
 */
public interface GeofenceContract {
    interface MapView {

        void setPresenter(Presenter presenter);

        void updateGeofence(GeofenceData geofenceData);

        void setMockLocation(Location location);

        void setGeofencingStarted(boolean started);
    }

    interface ControlsView {

        void setPresenter(Presenter presenter);

        void updateGeofence(GeofenceData geofenceData);

        void setTransitionType(int transitionType);

        void setGeofencingStarted(boolean started);

    }

    interface Presenter {

        void start();

        void stop();

        void updateGeofenceFromMap(GeofenceData geofenceData);

        void startGeofencing();

        void stopGeofencing();

        void setRandomMockLocation();

        void setCurrentWiFi();

        void updateGeofenceFromControls(GeofenceData geofenceData);

        GeofenceData getGeofenceData();

        void saveInstanceState(Bundle outState);

        void updateGeofenceAddedState();
    }
}
