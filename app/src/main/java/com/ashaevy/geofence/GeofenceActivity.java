package com.ashaevy.geofence;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Main activity.
 */
public class GeofenceActivity extends AppCompatActivity {

    private GeofencePresenter mGeofencePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence);

        GeofenceContract.MapView mapView = ((GeofenceContract.MapView) getSupportFragmentManager().
                findFragmentById(R.id.map));
        GeofenceContract.ControlsView controlsView = ((GeofenceContract.ControlsView)
                getSupportFragmentManager().findFragmentById(R.id.controls));

        DialogsFragment dialogsFragment = ((DialogsFragment) getSupportFragmentManager().
                findFragmentByTag(Constants.DIALOGS_FRAGMENT_TAG));
        if (dialogsFragment == null) {
            dialogsFragment = new DialogsFragment();
            getSupportFragmentManager().beginTransaction().add(dialogsFragment,
                    Constants.DIALOGS_FRAGMENT_TAG).commit();
        }

        mGeofencePresenter = new GeofencePresenter(this, mapView, controlsView,
                dialogsFragment, savedInstanceState);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGeofencePresenter.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mGeofencePresenter.saveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        mGeofencePresenter.stop();
        super.onStop();
    }

}
