package com.ashaevy.geofence;

import android.location.Location;

import com.ashaevy.geofence.data.GeofenceData;

/**
 * Contract between Views and Presenters.
 */
public interface GeofenceContract {
    interface MapView extends BaseView<Presenter> {

        void updateGeofence(GeofenceData geofenceData);

        void setLocation(Location location);
    }

    interface ControlsView extends BaseView<Presenter> {

        void updateGeofence(GeofenceData geofenceData);

        void setTransitionType(int transitionType);

    }

    interface Presenter extends BasePresenter {

        void stop();

        void updateGeofenceFromMap(GeofenceData geofenceData);

        void startGeofencing();

        void stopGeofencing();

        void setRandomMockLocation();

        void setCurrentWiFi();
    }
}
