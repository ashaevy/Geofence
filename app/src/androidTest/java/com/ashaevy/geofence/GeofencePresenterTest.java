package com.ashaevy.geofence;

import com.ashaevy.geofence.data.GeofenceData;
import com.ashaevy.geofence.data.source.GeofenceDataSource;
import com.ashaevy.geofence.transition.GeofenceHelper;
import com.google.android.gms.maps.model.LatLng;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test presenter.
 */

public class GeofencePresenterTest {

    private GeofenceDataSource mDataSource;

    private GeofenceContract.MapView mMapView;

    private GeofenceContract.DialogsView mDialogsView;

    private GeofenceContract.ControlsView mControlsView;

    private GeofenceHelper mGeofenceHelper;

    private GeofencePresenter mPresenter;

    @Before
    public void setupMocksAndView() {
        mDataSource = mock(GeofenceDataSource.class);
        mMapView = mock(GeofenceContract.MapView.class);
        mDialogsView = mock(GeofenceContract.DialogsView.class);
        mControlsView = mock(GeofenceContract.ControlsView.class);
        mGeofenceHelper = mock(GeofenceHelper.class);
    }

    @Test
    public void reportPermissionError_updateDataInViews() {
        mPresenter = new GeofencePresenter(mDataSource, mMapView, mControlsView,
                mDialogsView, null, mGeofenceHelper);

        mPresenter.reportPermissionError(-10);

        verify(mDialogsView).requestLocationPermission(-10);

        verify(mDataSource).saveGeofenceAdded(false);

        verify(mControlsView, times(2)).setGeofencingStarted(false);
        verify(mMapView, times(2)).setGeofencingStarted(false);
    }

    @Test
    public void updateGeofenceFromMap_changeAllExceptWifi() {
        mPresenter = new GeofencePresenter(mDataSource, mMapView, mControlsView,
                mDialogsView, null, mGeofenceHelper);

        mPresenter.getGeofenceData().setWifiName("test");
        String wifiName = mPresenter.getGeofenceData().getWifiName();

        GeofenceData geofenceData = new GeofenceData();
        geofenceData.setLatitude(10.0d);
        geofenceData.setLongitude(20.0d);
        geofenceData.setRadius(30.0d);
        mPresenter.updateGeofenceFromMap(geofenceData);

        Assert.assertEquals(mPresenter.getGeofenceData().getLatitude(), 10.0d);
        Assert.assertEquals(mPresenter.getGeofenceData().getLongitude(), 20.0d);
        Assert.assertEquals(mPresenter.getGeofenceData().getRadius(), 30.0d);
        Assert.assertEquals(mPresenter.getGeofenceData().getWifiName(), wifiName);


        verify(mControlsView).updateGeofence(mPresenter.getGeofenceData());

    }

    @Test
    public void startGeofencing_storeData() {
        mPresenter = new GeofencePresenter(mDataSource, mMapView, mControlsView,
                mDialogsView, null, mGeofenceHelper);

        mPresenter.startGeofencing();

        verify(mDataSource).saveGeofenceData(mPresenter.getGeofenceData());
    }

    @Test
    public void startGeofencing_callHelper() {
        mPresenter = new GeofencePresenter(mDataSource, mMapView, mControlsView,
                mDialogsView, null, mGeofenceHelper);

        mPresenter.startGeofencing();

        GeofenceData geofenceData = mPresenter.getGeofenceData();
        LatLng point = new LatLng(geofenceData.getLatitude(), geofenceData.getLongitude());
        verify(mGeofenceHelper).addGeofence(point, geofenceData.getRadius());
    }

    @Test
    public void stopGeofencing_updateState() {
        mPresenter = new GeofencePresenter(mDataSource, mMapView, mControlsView,
                mDialogsView, null, mGeofenceHelper);

        mPresenter.stopGeofencing();

        verify(mGeofenceHelper).removeGeofence();
        verify(mControlsView).setGeofenceState(Constants.GEOFENCE_STATE_UNKNOWN);
    }

}
