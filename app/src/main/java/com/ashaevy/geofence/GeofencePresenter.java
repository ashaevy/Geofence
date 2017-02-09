package com.ashaevy.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.ashaevy.geofence.data.GeofenceData;
import com.ashaevy.geofence.data.source.GeofenceDataSource;
import com.ashaevy.geofence.data.source.SPGeofenceDataSource;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Synchronize Map and Controls. Listen to geofense transition changes. Updates Views.
 */
public class GeofencePresenter implements GeofenceContract.Presenter {

    public static final int UNKNOWN_TRANSITION_TYPE = -1;

    private final Context mContext;

    private final GeofenceContract.MapView mMapView;
    private final GeofenceContract.ControlsView mControlsView;

    private final GeofenceHelper mGeofenceHelper;

    private GeofenceData mCurrentGeofenceData;
    private int mCurrentTransitionType;

    private String KEY_LAT = "KEY_LAT";
    private String KEY_LON = "KEY_LON";
    private String KEY_R = "KEY_R";
    private String KEY_WIFI = "KEY_WIFI";
    private String KEY_TRANSITION = "KEY_TRANSITION";

    private boolean mGeofencesAdded;
    private final GeofenceDataSource mGeofenceDataSource;

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
            mCurrentTransitionType = savedInstanceState.getInt(KEY_TRANSITION);
        } else {
            GeofenceData storedGeofenceData = mGeofenceDataSource.readGeofenceData();
            if (storedGeofenceData != null) {
                mCurrentGeofenceData = storedGeofenceData;
            } else {
                generateDefaultGeofence();
            }

        }

        mCurrentTransitionType = UNKNOWN_TRANSITION_TYPE;

        mGeofenceHelper = new GeofenceHelper(context, this);
        mGeofenceHelper.create();

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
        filter.addAction(GeofenceTransitionsIntentService.GEOFENCE_UPDATED);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(geofenceUpdateReceiver,
                filter);
        mGeofenceHelper.start();
    }

    @Override
    public void stop() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(geofenceUpdateReceiver);
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
        LatLng position = new LatLng(mCurrentGeofenceData.getLatitude(),
                mCurrentGeofenceData.getLongitude());
        mGeofenceHelper.addGeofence("GEOFENCE_CIRCLE",
                position, ((float) mCurrentGeofenceData.getRadius()));

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
        mCurrentTransitionType = UNKNOWN_TRANSITION_TYPE;
        mControlsView.setTransitionType(UNKNOWN_TRANSITION_TYPE);
    }

    @Override
    public void setRandomMockLocation() {
        Location mockLocation = generateRandomTestLocation();
        mGeofenceHelper.setMockLocation(mockLocation);
        mMapView.setMockLocation(mockLocation);
    }

    @Override
    public void setCurrentWiFi() {
        String currentSsid = getCurrentSsid(mContext);
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

    private BroadcastReceiver geofenceUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mCurrentTransitionType = intent.getIntExtra(GeofenceTransitionsIntentService.
                    KEY_GEOFENCE_UPDATE_TYPE, -1);
            mControlsView.setTransitionType(mCurrentTransitionType);
        }
    };

    private static String getCurrentSsid(Context context) {
        String ssid = null;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) {
            return null;
        }

        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
        }

        return ssid;
    }

    @Override
    public void saveInstanceState(Bundle outState) {
        outState.putDouble(KEY_LAT, mCurrentGeofenceData.getLatitude());
        outState.putDouble(KEY_LON, mCurrentGeofenceData.getLongitude());
        outState.putDouble(KEY_R, mCurrentGeofenceData.getRadius());
        outState.putString(KEY_WIFI, mCurrentGeofenceData.getWifiName());

        outState.putInt(KEY_TRANSITION, mCurrentTransitionType);
    }

    @Override
    public void updateGeofenceAddedState() {
        // Update state and save in shared preferences.
        setGeofenceAdded(!mGeofencesAdded);

        mGeofenceDataSource.saveGeofenceAdded(mGeofencesAdded);

        Toast.makeText(
                mContext,
                mContext.getString(mGeofencesAdded ? R.string.geofences_added :
                        R.string.geofences_removed),
                Toast.LENGTH_SHORT
        ).show();
    }
}
