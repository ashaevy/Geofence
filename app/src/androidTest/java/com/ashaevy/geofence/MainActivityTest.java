package com.ashaevy.geofence;

import android.graphics.Rect;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Testing Geofence activity.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    private UiDevice mDevice;

    @Before
    public void setUp() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void clearText_showsErrorText() {
        allowPermissionsIfNeeded();

        onView(withId(R.id.input_point_lat)).
                perform(clearText());

        onView(withId(R.id.textinput_error))
                .check(matches(isDisplayed()));
    }

    @Test
    public void invalidText_showsErrorDialog() {
        allowPermissionsIfNeeded();

        onView(withId(R.id.input_point_lat)).
                perform(clearText());

        onView(withId(R.id.start_geofencing)).
                perform(click());

        onView(withText(InstrumentationRegistry.getTargetContext().getString(R.
                string.validation_error))).check(matches(isDisplayed()));

    }

    @Test
    public void testMoveCenterMarker_changingEdits() throws
            UiObjectNotFoundException, InterruptedException {
        allowPermissionsIfNeeded();

        UiObject inputPointLat = mDevice.findObject(new UiSelector().
                resourceId("com.ashaevy.geofence:id/input_point_lat"));
        UiObject inputPointLon = mDevice.findObject(new UiSelector().
                resourceId("com.ashaevy.geofence:id/input_point_lon"));
        UiObject inputRadius = mDevice.findObject(new UiSelector().
                resourceId("com.ashaevy.geofence:id/input_radius"));

        String oldLat = inputPointLat.getText();
        String oldLon = inputPointLon.getText();
        String oldRadius = inputRadius.getText();

        setupMapFragment();

        // move center marker on 10 px
        UiObject centerMarker = mDevice.findObject(new UiSelector().
                descriptionContains(GeofenceMapFragment.CENTER_MAP_MARKER));
        Rect visibleBounds = centerMarker.getVisibleBounds();
        centerMarker.dragTo(visibleBounds.centerX() + 10,
                visibleBounds.centerY() + 10, 10);

        // check that lat lon changed and radius is same
        assertNotEquals(inputPointLat.getText(), oldLat);
        assertNotEquals(inputPointLon.getText(), oldLon);
        assertEquals(inputRadius.getText(), oldRadius);

    }

    @Test
    public void testMoveRadiusMarker_changingEdits() throws
            UiObjectNotFoundException, InterruptedException {
        allowPermissionsIfNeeded();

        UiObject inputPointLat = mDevice.findObject(new UiSelector().
                resourceId("com.ashaevy.geofence:id/input_point_lat"));
        UiObject inputPointLon = mDevice.findObject(new UiSelector().
                resourceId("com.ashaevy.geofence:id/input_point_lon"));
        UiObject inputRadius = mDevice.findObject(new UiSelector().
                resourceId("com.ashaevy.geofence:id/input_radius"));

        String oldLat = inputPointLat.getText();
        String oldLon = inputPointLon.getText();
        String oldRadius = inputRadius.getText();

        setupMapFragment();

        // move center marker on 10 px
        UiObject radiusMarker = mDevice.findObject(new UiSelector().
                descriptionContains(GeofenceMapFragment.RADIUS_MAP_MARKER));
        Rect visibleBounds = radiusMarker.getVisibleBounds();
        radiusMarker.dragTo(visibleBounds.centerX() + 10,
                visibleBounds.centerY() + 10, 10);

        // check that lat lon not changed and radius is new
        assertEquals(inputPointLat.getText(), oldLat);
        assertEquals(inputPointLon.getText(), oldLon);
        assertNotEquals(inputRadius.getText(), oldRadius);

    }

    private void setupMapFragment() throws InterruptedException {
        // turn on showing marker names so that we can find them
        FragmentManager fragmentManager = mActivityTestRule.getActivity().
                getSupportFragmentManager();
        final GeofenceMapFragment geofenceMapFragment = (GeofenceMapFragment) fragmentManager.
                findFragmentById(R.id.content_frame).
                getChildFragmentManager().findFragmentById(R.id.map);

        mActivityTestRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                geofenceMapFragment.showMarkerTitles();
            }
        });

        // wait for UI changes
        Thread.sleep(200);
    }

    private void allowPermissionsIfNeeded()  {
        if (Build.VERSION.SDK_INT >= 23) {
            UiObject allowPermissions = mDevice.findObject(new UiSelector().text("Allow"));
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    Log.e( MainActivityTest.class.getSimpleName(),
                            "There is no permissions dialog to interact with ", e);
                }
            }
        }
    }

}
