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
import android.widget.Toast;

import com.ashaevy.geofence.data.GeofenceData;
import com.ashaevy.geofence.data.source.GeofenceDataSource;
import com.ashaevy.geofence.data.source.SPGeofenceDataSource;
import com.ashaevy.geofence.transition.GeofenceHelper;
import com.ashaevy.geofence.transition.GeofenceTransitionDetector;
import com.ashaevy.geofence.transition.LocationBasedGeofenceHelper;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

/**
 * Synchronize Map and Controls. Listen to geofense state changes. Updates Views.
 */
public class GeofencePresenter implements GeofenceContract.Presenter {

    private final Context mContext;

    private final GeofenceContract.MapView mMapView;
    private final GeofenceContract.ControlsView mControlsView;

    private final GeofenceHelper mGeofenceHelper;

    private GeofenceData mCurrentGeofenceData;
    private int mCurrentGeofenceState;

    private String KEY_LAT = "KEY_LAT";
    private String KEY_LON = "KEY_LON";
    private String KEY_R = "KEY_R";
    private String KEY_WIFI = "KEY_WIFI";
    private String KEY_GEOFENCE_STATE = "KEY_GEOFENCE_STATE";

    private boolean mGeofencesAdded;
    private final GeofenceDataSource mGeofenceDataSource;

    private NetworkReceiver mNetworkUpdateReceiver;

    public GeofencePresenter(Context context, GeofenceContract.MapView mapView,
                             GeofenceContract.ControlsView controlsView, Bundle savedInstanceState) {
        mMapView = mapView;
        mControlsView = controlsView;
        mContext = context;

        mGeofenceDataSource = new SPGeofenceDataSource(mContext);

        mGeofencesAdded = mGeofenceDataSource.geofenceAdded();
        setGeofenceAdded(mGeofencesAdded);


        mCurrentGeofenceData = new GeofenceData();
        if (savedInstanceState != null) {
            mCurrentGeofenceData.setLatitude(savedInstanceState.getDouble(KEY_LAT));
            mCurrentGeofenceData.setLongitude(savedInstanceState.getDouble(KEY_LON));
            mCurrentGeofenceData.setRadius(savedInstanceState.getDouble(KEY_R));
            mCurrentGeofenceData.setWifiName(savedInstanceState.getString(KEY_WIFI));
            mCurrentGeofenceState = savedInstanceState.getInt(KEY_GEOFENCE_STATE);
        } else {
            GeofenceData storedGeofenceData = mGeofenceDataSource.readGeofenceData();
            if (storedGeofenceData != null) {
                mCurrentGeofenceData = storedGeofenceData;
            } else {
                generateDefaultGeofence();
            }

        }

        mCurrentGeofenceState = Constants.GEOFENCE_STATE_UNKNOWN;

//        mGeofenceHelper = new GooglePlayGeofenceHelper(context, this);
        mGeofenceHelper = new LocationBasedGeofenceHelper(context, this);
        mGeofenceHelper.create(savedInstanceState);

        mMapView.setPresenter(this);
        mControlsView.setPresenter(this);
    }

    private void generateDefaultGeofence() {
        mCurrentGeofenceData.setLatitude(Constants.KIEV.latitude);
        mCurrentGeofenceData.setLongitude(Constants.KIEV.longitude);
        mCurrentGeofenceData.setRadius(Constants.DEFAULT_RADIUS);
    }

    @Override
    public void start() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(GeofenceTransitionDetector.GEOFENCE_UPDATED);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mGeofenceUpdateReceiver,
                filter);
        if (mGeofencesAdded) {
            registerNetworkReceiver();
        }
        mGeofenceHelper.start();
    }

    private void registerNetworkReceiver() {
        mNetworkUpdateReceiver = new NetworkReceiver();
        IntentFilter networkFilter = new IntentFilter();
        networkFilter.addAction(CONNECTIVITY_ACTION);
        mContext.registerReceiver(mNetworkUpdateReceiver, networkFilter);
    }

    @Override
    public void stop() {
        unregisterReceiver();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mGeofenceUpdateReceiver);
        mGeofenceHelper.stop();
    }

    private void unregisterReceiver() {
        if (mNetworkUpdateReceiver != null) {
            mContext.unregisterReceiver(mNetworkUpdateReceiver);
            mNetworkUpdateReceiver = null;
        }
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
        mGeofenceHelper.enableMockLocation();
        LatLng position = new LatLng(mCurrentGeofenceData.getLatitude(),
                mCurrentGeofenceData.getLongitude());
        mGeofenceHelper.addGeofence(position, mCurrentGeofenceData.getRadius());

        mGeofenceDataSource.saveGeofenceData(mCurrentGeofenceData);
    }

    private void setGeofenceAdded(boolean added) {
        mGeofencesAdded = added;

        mControlsView.setGeofencingStarted(added);
        mMapView.setGeofencingStarted(added);
    }

    @Override
    public void stopGeofencing() {
        mGeofenceHelper.disableMockLocation();
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
    public void setCurrentWiFi() {
        String currentSsid = NetworkUtils.getCurrentSsid(mContext);
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
            mCurrentGeofenceState = intent.getIntExtra(GeofenceTransitionDetector.
                    KEY_GEOFENCE_UPDATE_TYPE, Constants.GEOFENCE_STATE_UNKNOWN);
            mControlsView.setGeofenceState(mCurrentGeofenceState);
        }
    };

    private class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CONNECTIVITY_ACTION.equals(intent.getAction()) && mGeofencesAdded) {
                mGeofenceHelper.notifyAboutNetworkChange();
            }
        }
    }

    @Override
    public void saveInstanceState(Bundle outState) {
        outState.putDouble(KEY_LAT, mCurrentGeofenceData.getLatitude());
        outState.putDouble(KEY_LON, mCurrentGeofenceData.getLongitude());
        outState.putDouble(KEY_R, mCurrentGeofenceData.getRadius());
        outState.putString(KEY_WIFI, mCurrentGeofenceData.getWifiName());

        outState.putInt(KEY_GEOFENCE_STATE, mCurrentGeofenceState);

        mGeofenceHelper.saveInstanceState(outState);
    }

    @Override
    public void updateGeofenceAddedState() {
        // Update state and save in shared preferences.
        setGeofenceAdded(!mGeofencesAdded);

        if (mGeofencesAdded) {
            registerNetworkReceiver();
        } else {
            unregisterReceiver();
        }

        mGeofenceDataSource.saveGeofenceAdded(mGeofencesAdded);

        Toast.makeText(
                mContext,
                mContext.getString(mGeofencesAdded ? R.string.geofences_added :
                        R.string.geofences_removed),
                Toast.LENGTH_SHORT
        ).show();
    }
}
