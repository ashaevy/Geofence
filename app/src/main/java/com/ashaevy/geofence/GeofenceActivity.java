package com.ashaevy.geofence;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ashaevy.geofence.data.source.GeofenceDataSource;
import com.ashaevy.geofence.transition.LocationBasedGeofenceHelper;

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

        GeofenceDataSource geofenceDataSource = Injection.provideGeofenceDataSource(this);
        LocationBasedGeofenceHelper geofenceHelper = new LocationBasedGeofenceHelper(this);

        mGeofencePresenter = new GeofencePresenter(geofenceDataSource, mapView, controlsView,
                dialogsFragment, savedInstanceState, geofenceHelper);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGeofencePresenter.start(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mGeofencePresenter.saveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        mGeofencePresenter.stop(this);
        super.onStop();
    }

}
