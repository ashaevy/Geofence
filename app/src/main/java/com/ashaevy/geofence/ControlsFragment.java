package com.ashaevy.geofence;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ashaevy.geofence.data.GeofenceData;
import com.google.android.gms.location.Geofence;

public class ControlsFragment extends Fragment implements GeofenceContract.ControlsView {

    private GeofenceContract.Presenter mPresenter;
    private TextInputEditText mPointXInput;
    private TextInputEditText mPointYInput;
    private TextInputEditText mRadiusInput;
    private TextInputEditText mWiFiNameInput;
    private View mStartGeofencingButton;
    private View mStopGeofencingButton;

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
            }
        });

        mStartGeofencingButton = view.findViewById(R.id.start_geofencing);
        mStopGeofencingButton = view.findViewById(R.id.stop_geofencing);

        mPointXInput = (TextInputEditText) view.findViewById(R.id.input_point_x);
        mPointYInput = ((TextInputEditText) view.findViewById(R.id.input_point_y));
        mRadiusInput = ((TextInputEditText) view.findViewById(R.id.input_radius));
        mWiFiNameInput = ((TextInputEditText) view.findViewById(R.id.input_wifi_name));

        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                requestUpdatePresenter();
            }
        };
        mPointXInput.setOnFocusChangeListener(onFocusChangeListener);
        mPointYInput.setOnFocusChangeListener(onFocusChangeListener);
        mRadiusInput.setOnFocusChangeListener(onFocusChangeListener);
        mWiFiNameInput.setOnFocusChangeListener(onFocusChangeListener);

        mStartGeofencingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.startGeofencing();
            }
        });

        mStopGeofencingButton.setOnClickListener(new View.OnClickListener() {
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
        mPointXInput.setText(String.valueOf(geofenceData.getLatitude()));
        mPointYInput.setText(String.valueOf(geofenceData.getLongitude()));
        mRadiusInput.setText(String.valueOf(geofenceData.getRadius()));
        mWiFiNameInput.setText(geofenceData.getWifiName());
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

    @Override
    public void setGeofencingStarted(boolean started) {
        mPointXInput.setEnabled(!started);
        mPointYInput.setEnabled(!started);
        mRadiusInput.setEnabled(!started);
        mWiFiNameInput.setEnabled(!started);

        mStartGeofencingButton.setEnabled(!started);
        mStopGeofencingButton.setEnabled(started);
    }

    @Override
    public void requestUpdatePresenter() {
        //TODO add validation
        //TODO add formatting

        GeofenceData geofenceData = new GeofenceData();
        geofenceData.setLatitude(Double.parseDouble(mPointXInput.getText().toString()));
        geofenceData.setLongitude(Double.parseDouble(mPointYInput.getText().toString()));
        geofenceData.setRadius(Double.parseDouble(mRadiusInput.getText().toString()));
        Editable text = mWiFiNameInput.getText();
        if (!TextUtils.isEmpty(text)) {
            geofenceData.setWifiName(text.toString());
        } else {
            geofenceData.setWifiName(null);
        }
        mPresenter.updateGeofenceFromControls(geofenceData);
    }

}
