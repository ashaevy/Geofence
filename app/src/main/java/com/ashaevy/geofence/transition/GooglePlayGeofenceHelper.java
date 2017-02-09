package com.ashaevy.geofence.transition;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.ashaevy.geofence.Constants;
import com.ashaevy.geofence.GeofenceContract;
import com.ashaevy.geofence.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;

/**
 * This class contains all helper code to setup receiving geofence transition events from
 * Google Play Geofence API.
 */
public class GooglePlayGeofenceHelper implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status>, GeofenceHelper {

    protected static final String TAG = "GeofenceHelper";
    public static final String DEFAULT_GEOFENCE_NAME = "GEOFENCE_CIRCLE";

    private final Context mContext;
    private final GeofenceContract.Presenter mPresenter;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;

    public GooglePlayGeofenceHelper(Context context, GeofenceContract.Presenter presenter) {
        mContext = context;
        mPresenter = presenter;
    }

    @Override
    public void create(Bundle savedInstanceState) {

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        // Kick off the request to build GoogleApiClient.
        buildGoogleApiClient();

    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void enableMockLocation() {
        try {
            LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);
        } catch (SecurityException e) {
            logSecurityException(e);
        }
    }

    @Override
    public void disableMockLocation() {
        try {
            LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, false);
        } catch (SecurityException e) {
            logSecurityException(e);
        }
    }

    @Override
    public void notifyAboutNetworkChange() {
        Intent startIntent = new Intent(mContext,
                GeofenceTransitionsIntentService.class);
        startIntent.setAction(CONNECTIVITY_ACTION);
        mContext.startService(startIntent);
    }

    @Override
    public void setMockLocation(Location location) {
        try {
            LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient, location);
        } catch (SecurityException e) {
            logSecurityException(e);
        }
    }

    @Override
    public void start() {
        mGoogleApiClient.connect();
    }

    @Override
    public void saveInstanceState(Bundle outState) {

    }

    @Override
    public void stop() {
        mGoogleApiClient.disconnect();
    }

    @Override
    public void addGeofence(LatLng position, double radius) {
        addGeofence(DEFAULT_GEOFENCE_NAME,
                position, ((float) radius));
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        Log.i(TAG, "Connection suspended");

        // onConnected() will be called again automatically when the service reconnects
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the geofence to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest(String requestId, LatLng point, float radius) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofence(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(requestId)

                // Set the circular region of this geofence.
                .setCircularRegion(
                        point.latitude,
                        point.longitude,
                        radius
                )

                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(NEVER_EXPIRE)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)

                // Create the geofence.
                .build());

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Adds geofence, which sets alerts to be notified when the device enters or exits the
     * specified geofence. Handles the success or failure results returned by addGeofences().
     */
    public void addGeofence(String requestId, LatLng point, float radius) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(mContext, mContext.getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(requestId, point, radius),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } else {
            mPresenter.reportPermissionError(Constants.LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    @Override
    public void removeGeofence() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(mContext, mContext.getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } else {
            mPresenter.reportPermissionError(Constants.LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    /**
     * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
     * Either method can complete successfully or with an error.
     *
     * Since this activity implements the {@link ResultCallback} interface, we are required to
     * define this method.
     *
     * @param status The Status returned through a PendingIntent when addGeofences() or
     *               removeGeofences() get called.
     */
    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            mPresenter.updateGeofenceAddedState(!mPresenter.geofenceAdded());
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(mContext,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
