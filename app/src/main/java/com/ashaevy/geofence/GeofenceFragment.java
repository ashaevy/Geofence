package com.ashaevy.geofence;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ashaevy.geofence.data.source.GeofenceDataSource;
import com.ashaevy.geofence.transition.LocationBasedGeofenceHelper;

/**
 * Fragment that contains main application UI.
 */
public class GeofenceFragment extends Fragment {

    private GeofencePresenter mGeofencePresenter;

    public GeofenceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_geofence, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GeofenceContract.MapView mapView = ((GeofenceContract.MapView) getChildFragmentManager().
                findFragmentById(R.id.map));
        GeofenceContract.ControlsView controlsView = ((GeofenceContract.ControlsView)
                getChildFragmentManager().findFragmentById(R.id.controls));

        DialogsFragment dialogsFragment = ((DialogsFragment) getFragmentManager().
                findFragmentByTag(Constants.DIALOGS_FRAGMENT_TAG));
        if (dialogsFragment == null) {
            dialogsFragment = new DialogsFragment();
            getChildFragmentManager().beginTransaction().add(dialogsFragment,
                    Constants.DIALOGS_FRAGMENT_TAG).commit();
        }

        GeofenceDataSource geofenceDataSource = Injection.provideGeofenceDataSource(getActivity());
        LocationBasedGeofenceHelper geofenceHelper = new LocationBasedGeofenceHelper(getActivity());

        GeofencePresenter.Views views = new GeofencePresenter.Views(mapView,
                controlsView, dialogsFragment);

        mGeofencePresenter = new GeofencePresenter(geofenceDataSource, views,
                geofenceHelper);
        mGeofencePresenter.create(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGeofencePresenter.start(getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mGeofencePresenter.saveInstanceState(outState);
    }

    @Override
    public void onStop() {
        mGeofencePresenter.stop(getActivity());
        super.onStop();
    }
}