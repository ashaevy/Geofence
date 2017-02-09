package com.ashaevy.geofence;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.ashaevy.geofence.data.GeofenceData;
import com.ashaevy.geofence.data.source.GeofenceDataSource;
import com.ashaevy.geofence.data.source.SPGeofenceDataSource;
import com.google.android.gms.location.Geofence;

/**
 * Created by ashaevy on 09.02.17.
 */

public class GeofenceTransitionDetector {

    public static final String GEOFENCE_UPDATED = GeofenceTransitionsIntentService.class.getName() + ".GEOFENCE_UPDATED";
    public static final String KEY_GEOFENCE_UPDATE_TYPE = "KEY_GEOFENCE_UPDATE_TYPE";

    private Context mContext;

    public GeofenceTransitionDetector(Context context) {
        mContext = context;
    }

    public void detectTransition() {
        int geofenceState = Constants.GEOFENCE_STATE_UNKNOWN;
        GeofenceDataSource dataSource = new SPGeofenceDataSource(mContext);

        int geofenceTransition = dataSource.readGeofenceTransition();

        GeofenceData geofenceData = dataSource.readGeofenceData();
        if (geofenceData != null) {
            String wifiName = geofenceData.getWifiName();
            String currentSsid = NetworkUtils.getCurrentSsid(mContext);
            if ((!TextUtils.isEmpty(wifiName) && wifiName.equals(currentSsid)) ||
                    (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)) {
                geofenceState = Constants.GEOFENCE_STATE_INSIDE;
            } else {
                geofenceState = Constants.GEOFENCE_STATE_OUTSIDE;
            }
        }

        // Send broadcast
        Intent updateIntent = new Intent(GEOFENCE_UPDATED);
        updateIntent.putExtra(KEY_GEOFENCE_UPDATE_TYPE, geofenceState);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(updateIntent);
    }

}
