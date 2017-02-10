package com.ashaevy.geofence;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Testing Geofence activity.
 */
@RunWith(AndroidJUnit4.class)
public class GeofenceActivityTest {
    @Rule
    public ActivityTestRule<GeofenceActivity> mActivityTestRule =
            new ActivityTestRule<>(GeofenceActivity.class);

    @Test
    public void clearText_showsErrorText() {
        onView(withId(R.id.input_point_x)).
                perform(clearText());

        onView(withId(R.id.textinput_error))
                .check(matches(isDisplayed()));
    }

}
