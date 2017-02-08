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
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.ashaevy.geofence.data.GeofenceData;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by ashaevy on 08.02.17.
 */
public class GeofencePresenter implements GeofenceContract.Presenter {

    public static final int UNKNOWN_TRANSITION_TYPE = -1;

    private final Context mContext;

    private final GeofenceContract.MapView mMapView;
    private final GeofenceContract.ControlsView mControlsView;

    private final GeofenceHelper mGeofenceHelper;

    private GeofenceData mCurrentGeofenceData;
    private int mCurrentTransitionType;

    public GeofencePresenter(Context context, GeofenceContract.MapView mapView,
                             GeofenceContract.ControlsView controlsView) {
        mMapView = mapView;
        mControlsView = controlsView;
        mContext = context;

        mMapView.setPresenter(this);
        mControlsView.setPresenter(this);

        mCurrentGeofenceData = new GeofenceData();
        mCurrentTransitionType = UNKNOWN_TRANSITION_TYPE;

        mGeofenceHelper = new GeofenceHelper(context);
        mGeofenceHelper.create();

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
        mGeofenceHelper.setGeofence("GEOFENCE_CIRCLE",
                position, ((float) mCurrentGeofenceData.getRadius()));
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
        mMapView.setLocation(mockLocation);
    }

    @Override
    public void setCurrentWiFi() {
        String currentSsid = getCurrentSsid(mContext);
        if (currentSsid != null) {
            mCurrentGeofenceData.setWifiName(currentSsid);
            mControlsView.updateGeofence(mCurrentGeofenceData);
        }
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
}
