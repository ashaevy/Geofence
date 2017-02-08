package com.ashaevy.geofence;

import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Display;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GeofenceActivity extends FragmentActivity implements
        OnMapReadyCallback, ControlsFragment.OnFragmentInteractionListener,
        GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener {

    private static final LatLng KIEV = new LatLng(50.4501, 30.5234);
    private static final double DEFAULT_RADIUS = 100;
    public static final double RADIUS_OF_EARTH_METERS = 6371009;

    public static final float DEFAULT_STROKE_WIDTH = 2;
    public static final int DEFAULT_FILL_COLOR = Color.parseColor("#4de95367");
    public static final int DEFAULT_STROKE_COLOR = Color.BLACK;

    private GoogleMap mMap;
    private DraggableCircle mGeofenceCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);

        mGeofenceCircle = new DraggableCircle(KIEV, DEFAULT_RADIUS, true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(KIEV, getZoomLevel(mGeofenceCircle.circle)));

        // FIXME add premission check
        mMap.setMyLocationEnabled(true);
    }

    public int getZoomLevel(Circle circle) {
        int zoomLevel = 11;
        if (circle != null) {
            double radius = circle.getRadius() + circle.getRadius() / 2;
            double scale = radius / 500;
            zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        onMarkerMoved(marker);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        onMarkerMoved(marker);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        onMarkerMoved(marker);
    }

    private void onMarkerMoved(Marker marker) {
        mGeofenceCircle.onMarkerMoved(marker);
        updateEdits();
    }

    private void updateEdits() {
        LatLng position = mGeofenceCircle.centerMarker.getPosition();
        ((TextInputEditText) findViewById(R.id.input_point_x)).setText(String.valueOf(position.latitude));
        ((TextInputEditText) findViewById(R.id.input_point_y)).setText(String.valueOf(position.longitude));
        ((TextInputEditText) findViewById(R.id.input_radius)).setText(String.valueOf(mGeofenceCircle.radius));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    private class DraggableCircle {
        private final Marker centerMarker;
        private final Marker radiusMarker;
        private final Circle circle;
        private double radius;

        public DraggableCircle(LatLng center, double radius, boolean clickable) {
            this.radius = radius;
            centerMarker = mMap.addMarker(new MarkerOptions()
                    .position(center)
                    .draggable(true));
            radiusMarker = mMap.addMarker(new MarkerOptions()
                    .position(toRadiusLatLng(center, radius))
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE)));
            circle = mMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(radius)
                    .strokeWidth(DEFAULT_STROKE_WIDTH)
                    .strokeColor(DEFAULT_STROKE_COLOR)
                    .fillColor(DEFAULT_FILL_COLOR)
                    .clickable(clickable));
        }

        public boolean onMarkerMoved(Marker marker) {
            if (marker.equals(centerMarker)) {
                circle.setCenter(marker.getPosition());
                radiusMarker.setPosition(toRadiusLatLng(marker.getPosition(), radius));
                return true;
            }
            if (marker.equals(radiusMarker)) {
                radius = toRadiusMeters(centerMarker.getPosition(), radiusMarker.getPosition());
                circle.setRadius(radius);
                return true;
            }
            return false;
        }

        public void setClickable(boolean clickable) {
            circle.setClickable(clickable);
        }
    }

    /** Generate LatLng of radius marker */
    private static LatLng toRadiusLatLng(LatLng center, double radius) {
        double radiusAngle = Math.toDegrees(radius / RADIUS_OF_EARTH_METERS) /
                Math.cos(Math.toRadians(center.latitude));
        return new LatLng(center.latitude, center.longitude + radiusAngle);
    }

    private static double toRadiusMeters(LatLng center, LatLng radius) {
        float[] result = new float[1];
        Location.distanceBetween(center.latitude, center.longitude,
                radius.latitude, radius.longitude, result);
        return result[0];
    }
}
