package com.ashaevy.geofence.transition;

import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.ashaevy.geofence.GeofenceContract;
import com.ashaevy.geofence.data.GeofenceData;
import com.ashaevy.geofence.data.source.GeofenceDataSource;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test showing use of mock location data to test code using the Google Play Location APIs. To
 * run this test, you must first check the "Allow mock locations" setting within
 * Settings -> Developer options.
 */
public class LocationBasedGeofenceHelperTest {
    public static final String TAG = "GeofenceHelperTest";

    /**
     * The name of the mock location.
     */
    public static final String NORTH_POLE =
            "com.ashaevy.geofence.transition" + ".NORTH_POLE";

    public static final float NORTH_POLE_LATITUDE = 90.0f;

    public static final float NORTH_POLE_LONGITUDE = 0.0f;

    public static final float ACCURACY_IN_METERS = 10.0f;

    public static final int AWAIT_TIMEOUT_IN_MILLISECONDS = 2000;

    private LocationBasedGeofenceHelper mLocationBasedGeofenceHelper;
    private GeofenceContract.Presenter mPresenter;
    private GeofenceDataSource mGeofenceDataSource;

    @Before
    public void setUp() {
        mGeofenceDataSource = mock(GeofenceDataSource.class);
        mLocationBasedGeofenceHelper = new LocationBasedGeofenceHelper(InstrumentationRegistry.
                getTargetContext(), mGeofenceDataSource);
        mPresenter = mock(GeofenceContract.Presenter.class);

        // location near NORTH_POLE
        GeofenceData geofenceData = new GeofenceData();
        geofenceData.setLatitude(NORTH_POLE_LATITUDE);
        geofenceData.setLongitude(NORTH_POLE_LONGITUDE + 1.0d);
        geofenceData.setRadius(200000);
        geofenceData.setWifiName("test");
        when(mPresenter.getGeofenceData()).thenReturn(geofenceData);
        when(mGeofenceDataSource.readGeofenceData()).thenReturn(geofenceData);

        mLocationBasedGeofenceHelper.setPresenter(mPresenter);
        mLocationBasedGeofenceHelper.create(null);
        mLocationBasedGeofenceHelper.start();
        ensureGoogleApiClientConnection();
    }

    /**
     * Tests location using a mock location object.
     */
    @Test
    public void testUsingMockLocation() throws InterruptedException {
        Log.v(TAG, "Testing current location");

        // Use a Location object set to the coordinates of the North Pole to set the mock location.
        setMockLocation(createNorthPoleLocation());

        // Make sure that the activity under test exists and GoogleApiClient is connected.
        confirmPreconditions();

        new Handler(InstrumentationRegistry.getTargetContext().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mLocationBasedGeofenceHelper.startLocationUpdates();
            }
        });

        // wait for first location
        Thread.sleep(500);

        final Location testLocation = createNorthPoleLocation();

        // test that we mocked everything correct
        assertEquals(testLocation.getLatitude(),
                mLocationBasedGeofenceHelper.mCurrentLocation.getLatitude(),
                0.000001f);
        assertEquals(testLocation.getLongitude(),
                mLocationBasedGeofenceHelper.mCurrentLocation.getLongitude(),
                 0.000001f);

        // check if we stored correct transition
        verify(mGeofenceDataSource).saveGeofenceTransition(Geofence.GEOFENCE_TRANSITION_ENTER);
    }

    @After
    public void tearDown() {
        mLocationBasedGeofenceHelper.stop();
    }

    /**
     * If a connection to GoogleApiClient is lost, attempts to reconnect.
     */
    private void ensureGoogleApiClientConnection() {
        if (!mLocationBasedGeofenceHelper.mGoogleApiClient.isConnected()) {
            mLocationBasedGeofenceHelper.mGoogleApiClient.blockingConnect();
        }
    }

    /**
     * Confirms that the activity under test exists and has a connected GoogleApiClient.
     */
    private void confirmPreconditions() {
        assertNotNull("mLocationBasedGeofenceHelper is null", mLocationBasedGeofenceHelper);
        assertTrue("GoogleApiClient is not connected", mLocationBasedGeofenceHelper.
                mGoogleApiClient.isConnected());
    }

    private void setMockLocation(final Location mockLocation) {
        final CountDownLatch lock = new CountDownLatch(1);

        try {
            LocationServices.FusedLocationApi.
                    setMockMode(mLocationBasedGeofenceHelper.mGoogleApiClient, true)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                try {
                                    Log.v(TAG, "Mock mode set");
                                    LocationServices.FusedLocationApi.setMockLocation(
                                            mLocationBasedGeofenceHelper.mGoogleApiClient,
                                            mockLocation
                                    ).setResultCallback(new ResultCallback<Status>() {
                                        @Override
                                        public void onResult(Status status) {
                                            if (status.isSuccess()) {
                                                Log.v(TAG, "Mock location set");
                                                // Decrement the count of the latch, releasing the waiting
                                                // thread. This permits lock.await() to return.
                                                Log.v(TAG, "Decrementing latch count");
                                                lock.countDown();
                                            } else {
                                                Log.e(TAG, "Mock location not set");
                                            }
                                        }
                                    });
                                } catch (SecurityException e) {
                                    Log.e(TAG, "No permission to mock location", e);
                                    throw e;
                                }
                            } else {
                                Log.e(TAG, "Mock mode not set");
                            }
                        }
                    });

            try {
                Log.v(TAG, "Waiting until the latch has counted down to zero");
                lock.await(AWAIT_TIMEOUT_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException exception) {
                Log.i(TAG, "Waiting thread awakened prematurely", exception);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "No permission for mock mode", e);
            throw e;
        }

    }

    /**
     * Creates and returns a Location object set to the coordinates of the North Pole.
     */
    private Location createNorthPoleLocation() {
        Location mockLocation = new Location(NORTH_POLE);
        mockLocation.setLatitude(NORTH_POLE_LATITUDE);
        mockLocation.setLongitude(NORTH_POLE_LONGITUDE);
        mockLocation.setAccuracy(ACCURACY_IN_METERS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        mockLocation.setTime(System.currentTimeMillis());
        return mockLocation;
    }
}
