package com.ashaevy.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.ashaevy.geofence.data.GeofenceData;
import com.ashaevy.geofence.data.source.GeofenceDataSource;
import com.ashaevy.geofence.transition.GeofenceHelper;
import com.ashaevy.geofence.transition.GeofenceTransitionDetector;
import com.ashaevy.geofence.utils.NetworkUtils;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

/**
 * Synchronize Map and Controls. Listen to geofense state changes. Updates Views.
 */
public class GeofencePresenter implements GeofenceContract.Presenter {

    private final GeofenceHelper mGeofenceHelper;
    private final GeofenceDataSource mGeofenceDataSource;

    private final GeofenceContract.MapView mMapView;
    private final GeofenceContract.ControlsView mControlsView;
    private final GeofenceContract.DialogsView mDialogsView;

    private GeofenceData mCurrentGeofenceData;
    private int mCurrentGeofenceState;
    private boolean mGeofenceAdded;

    private String KEY_LAT = "KEY_LAT";
    private String KEY_LON = "KEY_LON";
    private String KEY_R = "KEY_R";
    private String KEY_WIFI = "KEY_WIFI";
    private String KEY_GEOFENCE_STATE = "KEY_GEOFENCE_STATE";
    private static final String KEY_GEOFENCE_ADDED = "KEY_GEOFENCE_ADDED";

    public GeofencePresenter(GeofenceDataSource geofenceDataSource,
                             Views views,
                             GeofenceHelper geofenceHelper) {

        mGeofenceDataSource = geofenceDataSource;

        mMapView = views.getMapView();
        mControlsView = views.getControlsView();
        mDialogsView = views.getDialogsView();

        mMapView.setPresenter(this);
        mControlsView.setPresenter(this);

        mGeofenceHelper = geofenceHelper;
        mGeofenceHelper.setPresenter(this);
    }

    public void create(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentGeofenceData = new GeofenceData();
            mCurrentGeofenceData.setLatitude(savedInstanceState.getDouble(KEY_LAT));
            mCurrentGeofenceData.setLongitude(savedInstanceState.getDouble(KEY_LON));
            mCurrentGeofenceData.setRadius(savedInstanceState.getDouble(KEY_R));
            mCurrentGeofenceData.setWifiName(savedInstanceState.getString(KEY_WIFI));
            mCurrentGeofenceState = savedInstanceState.getInt(KEY_GEOFENCE_STATE);
            mGeofenceAdded = savedInstanceState.getBoolean(KEY_GEOFENCE_ADDED);
        } else {
            mCurrentGeofenceData = mGeofenceDataSource.readGeofenceData();
            mCurrentGeofenceState = Constants.GEOFENCE_STATE_UNKNOWN;
            mGeofenceAdded = mGeofenceDataSource.geofenceAdded();
        }

        mGeofenceHelper.create(savedInstanceState);
    }

    @Override
    public void start(Context context) {
        updateGeofenceAddedUIState(mGeofenceAdded);
        mControlsView.updateGeofence(mCurrentGeofenceData);
        mMapView.updateGeofence(mCurrentGeofenceData);
        IntentFilter filter = new IntentFilter();
        filter.addAction(GeofenceTransitionDetector.GEOFENCE_UPDATED);
        LocalBroadcastManager.getInstance(context).registerReceiver(mGeofenceUpdateReceiver,
                filter);
        mGeofenceHelper.start();
    }


    @Override
    public void stop(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mGeofenceUpdateReceiver);
        mGeofenceHelper.stop();
    }

    @Override
    public void updateGeofenceFromMap(GeofenceData geofenceData) {
        mCurrentGeofenceData.setLatitude(geofenceData.getLatitude());
        mCurrentGeofenceData.setLongitude(geofenceData.getLongitude());
        mCurrentGeofenceData.setRadius(geofenceData.getRadius());

        mControlsView.updateGeofence(mCurrentGeofenceData);
    }

    @Override
    public void startGeofencing() {
        // start from outside position as first trigger event is entering
        mGeofenceDataSource.saveGeofenceTransition(Geofence.GEOFENCE_TRANSITION_EXIT);
        mGeofenceDataSource.saveGeofenceData(mCurrentGeofenceData);

        LatLng position = new LatLng(mCurrentGeofenceData.getLatitude(),
                mCurrentGeofenceData.getLongitude());
        mGeofenceHelper.addGeofence(position, mCurrentGeofenceData.getRadius());
    }

    private void updateGeofenceAddedUIState(boolean added) {
        mControlsView.setGeofencingStarted(added);
        mMapView.setGeofencingStarted(added);
    }

    @Override
    public void stopGeofencing() {
        mGeofenceHelper.removeGeofence();
        mCurrentGeofenceState = Constants.GEOFENCE_STATE_UNKNOWN;
        mControlsView.setGeofenceState(Constants.GEOFENCE_STATE_UNKNOWN);
    }

    @Override
    public void setRandomMockLocation() {
        Location mockLocation = generateRandomTestLocation();
        mGeofenceHelper.setMockLocation(mockLocation);
        mMapView.setMockLocation(mockLocation);
    }

    @Override
    public void setCurrentWiFi(Context context) {
        String currentSsid = NetworkUtils.getCurrentSsid(context);
        if (currentSsid != null) {
            mCurrentGeofenceData.setWifiName(currentSsid);
            mControlsView.updateGeofence(mCurrentGeofenceData);
        }
    }

    @Override
    public void updateGeofenceFromControls(GeofenceData geofenceData) {
        mCurrentGeofenceData.setLatitude(geofenceData.getLatitude());
        mCurrentGeofenceData.setLongitude(geofenceData.getLongitude());
        mCurrentGeofenceData.setRadius(geofenceData.getRadius());
        mCurrentGeofenceData.setWifiName(geofenceData.getWifiName());

        mMapView.updateGeofence(mCurrentGeofenceData);
    }

    @Override
    public GeofenceData getGeofenceData() {
        return mCurrentGeofenceData;
    }

    private Location generateRandomTestLocation() {
        Location location = new Location(LocationManager.NETWORK_PROVIDER);
        location.setLatitude(Constants.KIEV.latitude + Math.random() * 0.1);
        location.setLongitude(Constants.KIEV.longitude + Math.random() * 0.1);
        location.setTime(new Date().getTime());
        location.setAccuracy(3.0f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            location.setElapsedRealtimeNanos(System.nanoTime());
        }

        return location;
    }

    private BroadcastReceiver mGeofenceUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mGeofenceAdded) {
                mCurrentGeofenceState = intent.getIntExtra(GeofenceTransitionDetector.
                        KEY_GEOFENCE_UPDATE_TYPE, Constants.GEOFENCE_STATE_UNKNOWN);
                mControlsView.setGeofenceState(mCurrentGeofenceState);
            }
        }
    };

    @Override
    public void saveInstanceState(Bundle outState) {
        outState.putDouble(KEY_LAT, mCurrentGeofenceData.getLatitude());
        outState.putDouble(KEY_LON, mCurrentGeofenceData.getLongitude());
        outState.putDouble(KEY_R, mCurrentGeofenceData.getRadius());
        outState.putString(KEY_WIFI, mCurrentGeofenceData.getWifiName());

        outState.putBoolean(KEY_GEOFENCE_ADDED, mGeofenceAdded);
        outState.putInt(KEY_GEOFENCE_STATE, mCurrentGeofenceState);

        mGeofenceHelper.saveInstanceState(outState);
    }

    @Override
    public void updateGeofenceAddedState(boolean geofenceAdded) {
        mGeofenceAdded = geofenceAdded;
        updateGeofenceAddedUIState(geofenceAdded);
        mGeofenceDataSource.saveGeofenceAdded(mGeofenceAdded);
    }

    @Override
    public boolean geofenceAdded() {
        return mGeofenceAdded;
    }

    @Override
    public void reportPermissionError(int requestId) {
        updateGeofenceAddedState(false);
        mDialogsView.requestLocationPermission(requestId);
    }

    @Override
    public void reportNotReadyError() {
        mDialogsView.reportNotReadyError();
    }

    @Override
    public void reportErrorMessage(String errorMessage) {
        mDialogsView.reportErrorMessage(errorMessage);
    }

    public static class Views {
        private final GeofenceContract.MapView mMapView;
        private final GeofenceContract.ControlsView mControlsView;
        private final GeofenceContract.DialogsView mDialogsView;

        public Views(GeofenceContract.MapView mMapView, GeofenceContract.ControlsView mControlsView,
                     GeofenceContract.DialogsView mDialogsView) {
            this.mMapView = mMapView;
            this.mControlsView = mControlsView;
            this.mDialogsView = mDialogsView;
        }

        public GeofenceContract.MapView getMapView() {
            return mMapView;
        }

        public GeofenceContract.ControlsView getControlsView() {
            return mControlsView;
        }

        public GeofenceContract.DialogsView getDialogsView() {
            return mDialogsView;
        }
    }

}
