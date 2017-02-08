package com.ashaevy.geofence;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class GeofenceActivity extends FragmentActivity {

    private GeofencePresenter mGeofencePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence);

        GeofenceContract.MapView mapView = ((GeofenceContract.MapView) getSupportFragmentManager().
                findFragmentById(R.id.map));
        GeofenceContract.ControlsView controlsView = ((GeofenceContract.ControlsView)
                getSupportFragmentManager().findFragmentById(R.id.controls));

        mGeofencePresenter = new GeofencePresenter(this, mapView, controlsView);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGeofencePresenter.start();
    }

    @Override
    protected void onStop() {
        mGeofencePresenter.stop();
        super.onStop();
    }

}
