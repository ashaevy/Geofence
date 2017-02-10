package com.ashaevy.geofence.transition;

import android.location.Location;

import com.ashaevy.geofence.Constants;
import com.ashaevy.geofence.data.GeofenceData;
import com.ashaevy.geofence.data.source.GeofenceDataSource;
import com.google.android.gms.location.Geofence;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test main business logic.
 */

public class GeofenceTransitionDetectorTest {

    private GeofenceDataSource mDataSource;

    private GeofenceTransitionDetector mGeofenceTransitionDetector;

    @Before
    public void setupMocksAndView() {
        mDataSource = mock(GeofenceDataSource.class);
    }

    @Test
    public void detectTransitionState_insideGeofence() {

        when(mDataSource.readGeofenceTransition()).thenReturn(Geofence.GEOFENCE_TRANSITION_ENTER);
        GeofenceData geofenceData = new GeofenceData();
        geofenceData.setWifiName("some_ssid");
        when(mDataSource.readGeofenceData()).thenReturn(geofenceData);
        mGeofenceTransitionDetector = new GeofenceTransitionDetector(mDataSource);

        int state = mGeofenceTransitionDetector.detectTransitionState(new GeofenceTransitionDetector.SsidProvider() {
            @Override
            public String getSsid() {
                return "another_ssid";
            }
        });

        assertEquals(state, Constants.GEOFENCE_STATE_INSIDE);
    }

    @Test
    public void detectTransitionState_sameWiFi() {

        when(mDataSource.readGeofenceTransition()).thenReturn(Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofenceData geofenceData = new GeofenceData();
        geofenceData.setWifiName("same_ssid");
        when(mDataSource.readGeofenceData()).thenReturn(geofenceData);
        mGeofenceTransitionDetector = new GeofenceTransitionDetector(mDataSource);

        int state = mGeofenceTransitionDetector.detectTransitionState(new GeofenceTransitionDetector.SsidProvider() {
            @Override
            public String getSsid() {
                return "same_ssid";
            }
        });

        assertEquals(state, Constants.GEOFENCE_STATE_INSIDE);
    }

    @Test
    public void detectTransitionState_outside() {

        when(mDataSource.readGeofenceTransition()).thenReturn(Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofenceData geofenceData = new GeofenceData();
        geofenceData.setWifiName("some_ssid");
        when(mDataSource.readGeofenceData()).thenReturn(geofenceData);
        mGeofenceTransitionDetector = new GeofenceTransitionDetector(mDataSource);

        int state = mGeofenceTransitionDetector.detectTransitionState(new GeofenceTransitionDetector.SsidProvider() {
            @Override
            public String getSsid() {
                return "another_ssid";
            }
        });

        assertEquals(state, Constants.GEOFENCE_STATE_OUTSIDE);
    }

    @Test
    public void geofenceCoordinatesTransition_entered() {

        mGeofenceTransitionDetector = new GeofenceTransitionDetector(mDataSource);

        Location location = new Location("test");
        location.setLatitude(1.0d);
        location.setLongitude(1.0d);

        GeofenceData geofenceData = new GeofenceData();
        // dx = dy = 111 km
        geofenceData.setLatitude(0.0d);
        geofenceData.setLongitude(2.0d);
        // 200 km
        geofenceData.setRadius(200000d);

        int state = mGeofenceTransitionDetector.
                geofenceCoordinatesTransition(geofenceData, location);

        assertEquals(state, Geofence.GEOFENCE_TRANSITION_ENTER);
    }

    @Test
    public void geofenceCoordinatesTransition_exited() {

        mGeofenceTransitionDetector = new GeofenceTransitionDetector(mDataSource);

        Location location = new Location("test");
        location.setLongitude(2.0d);
        location.setLatitude(2.0d);

        GeofenceData geofenceData = new GeofenceData();
        // 111 km
        geofenceData.setLatitude(3.0d);
        geofenceData.setLongitude(2.0d);
        // 50 km
        geofenceData.setRadius(50000d);

        int state = mGeofenceTransitionDetector.
                geofenceCoordinatesTransition(geofenceData, location);

        assertEquals(state, Geofence.GEOFENCE_TRANSITION_EXIT);
    }
}
