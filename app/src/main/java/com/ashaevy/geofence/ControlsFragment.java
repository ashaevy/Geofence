package com.ashaevy.geofence;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ashaevy.geofence.data.GeofenceData;
import com.google.android.gms.location.Geofence;

public class ControlsFragment extends Fragment implements GeofenceContract.ControlsView {

    private GeofenceContract.Presenter mPresenter;

    public ControlsFragment() {
        // Required empty public constructor
    }

    @Override
    public void setPresenter(GeofenceContract.Presenter presenter) {
        mPresenter = presenter;
    }

    public static ControlsFragment newInstance() {
        return new ControlsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_controls, container, false);

        view.findViewById(R.id.button_set_current_wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.setCurrentWiFi();

                //FIXME

            }
        });

        view.findViewById(R.id.start_geofencing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.startGeofencing();
            }
        });

        view.findViewById(R.id.stop_geofencing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.stopGeofencing();
            }
        });

        view.findViewById(R.id.button_random_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.setRandomMockLocation();
            }
        });

        return view;
    }

    @Override
    public void updateGeofence(GeofenceData geofenceData) {
        View view = getView();
        if (view != null) {
            ((TextInputEditText) view.findViewById(R.id.input_point_x)).setText(String.valueOf(geofenceData.getLatitude()));
            ((TextInputEditText) view.findViewById(R.id.input_point_y)).setText(String.valueOf(geofenceData.getLongitude()));
            ((TextInputEditText) view.findViewById(R.id.input_radius)).setText(String.valueOf(geofenceData.getRadius()));
            ((TextInputEditText) view.findViewById(R.id.input_wifi_name)).setText(String.valueOf(geofenceData.getWifiName()));
        }
    }

    @Override
    public void setTransitionType(int transitionType) {
        View view = getView();
        if (view != null) {
            switch (transitionType) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    ((TextView) view.findViewById(R.id.geofence_state)).setText(R.string.geofence_state_inside);
                    return;
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    ((TextView) view.findViewById(R.id.geofence_state)).setText(R.string.geofence_state_outsize);
                    return;
                default:
                    ((TextView) view.findViewById(R.id.geofence_state)).setText(R.string.geofence_state_unknown);
            }
        }
    }

}
