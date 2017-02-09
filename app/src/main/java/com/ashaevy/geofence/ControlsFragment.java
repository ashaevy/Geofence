package com.ashaevy.geofence;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ashaevy.geofence.data.GeofenceData;

public class ControlsFragment extends Fragment implements GeofenceContract.ControlsView {

    private final String TAG = "ControlsFragment";

    private GeofenceContract.Presenter mPresenter;
    private TextInputEditText mPointXInput;
    private TextInputEditText mPointYInput;
    private TextInputEditText mRadiusInput;
    private TextInputEditText mWiFiNameInput;
    private View mStartGeofencingButton;
    private View mStopGeofencingButton;
    private View mSetCurrentWiFiButton;
    private InputWatcher mTextWatcher = new InputWatcher();

    public ControlsFragment() {
        // Required empty public constructor
    }

    @Override
    public void setPresenter(GeofenceContract.Presenter presenter) {
        mPresenter = presenter;

        // first setup
        updateGeofence(mPresenter.getGeofenceData());
    }

    public static ControlsFragment newInstance() {
        return new ControlsFragment();
    }

    private class InputWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            tryUpdatePresenterData(false);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_controls, container, false);

        mStartGeofencingButton = view.findViewById(R.id.start_geofencing);
        mStopGeofencingButton = view.findViewById(R.id.stop_geofencing);
        mSetCurrentWiFiButton = view.findViewById(R.id.button_set_current_wifi);

        mPointXInput = (TextInputEditText) view.findViewById(R.id.input_point_x);
        mPointYInput = ((TextInputEditText) view.findViewById(R.id.input_point_y));
        mRadiusInput = ((TextInputEditText) view.findViewById(R.id.input_radius));
        mWiFiNameInput = ((TextInputEditText) view.findViewById(R.id.input_wifi_name));

        registerTextWatcher();

        mSetCurrentWiFiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.setCurrentWiFi();
            }
        });

        mStartGeofencingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tryUpdatePresenterData(true)) {
                    mPresenter.startGeofencing();
                }
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

    private void registerTextWatcher() {
        mPointXInput.addTextChangedListener(mTextWatcher);
        mPointYInput.addTextChangedListener(mTextWatcher);
        mRadiusInput.addTextChangedListener(mTextWatcher);
        mWiFiNameInput.addTextChangedListener(mTextWatcher);
    }

    private void unregisterTextWatcher() {
        mPointXInput.removeTextChangedListener(mTextWatcher);
        mPointYInput.removeTextChangedListener(mTextWatcher);
        mRadiusInput.removeTextChangedListener(mTextWatcher);
        mWiFiNameInput.removeTextChangedListener(mTextWatcher);
    }

    @Override
    public void updateGeofence(GeofenceData geofenceData) {
        unregisterTextWatcher();

        mPointXInput.setText(String.valueOf(geofenceData.getLatitude()));
        mPointYInput.setText(String.valueOf(geofenceData.getLongitude()));
        mRadiusInput.setText(String.valueOf(geofenceData.getRadius()));
        mWiFiNameInput.setText(geofenceData.getWifiName());

        registerTextWatcher();
    }

    @Override
    public void setGeofenceState(int geofenceState) {
        View view = getView();
        if (view != null) {
            switch (geofenceState) {
                case Constants.GEOFENCE_STATE_INSIDE:
                    ((TextView) view.findViewById(R.id.geofence_state)).setText(R.string.geofence_state_inside);
                    return;
                case Constants.GEOFENCE_STATE_OUTSIDE:
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
        mSetCurrentWiFiButton.setEnabled(!started);
        mStopGeofencingButton.setEnabled(started);
    }

    public boolean tryUpdatePresenterData(boolean reportError) {
        try {
            GeofenceData geofenceData = new GeofenceData();
            geofenceData.setLatitude(doubleInputValidation(mPointXInput));
            geofenceData.setLongitude(doubleInputValidation(mPointYInput));
            geofenceData.setRadius(doubleInputValidation(mRadiusInput));

            Editable text = mWiFiNameInput.getText();
            if (!TextUtils.isEmpty(text)) {
                geofenceData.setWifiName(text.toString());
            } else {
                throw new ValidationException();
            }

            mPresenter.updateGeofenceFromControls(geofenceData);

            return true;
        } catch (ValidationException e) {

            if (reportError) {
                showErrorDialog();

                // revert data
                updateGeofence(mPresenter.getGeofenceData());
            }

            return false;
        }
    }

    private void showErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.validation_error)
                .setTitle(R.string.validation_error_dialog_title);

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private double doubleInputValidation(TextInputEditText doubleTextInputEditText) {
        Editable editable = doubleTextInputEditText.getText();
        if (TextUtils.isEmpty(editable)) {
            throw new ValidationException();
        }
        try {
            return Double.parseDouble(editable.toString());
        } catch (NumberFormatException e) {
            throw new ValidationException();
        }
    }

    private static class ValidationException extends RuntimeException {
        public ValidationException() {}

        public ValidationException(String message) {
            super(message);
        }

    }

}
