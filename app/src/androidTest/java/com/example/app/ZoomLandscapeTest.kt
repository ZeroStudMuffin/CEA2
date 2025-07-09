package com.example.app

import android.content.pm.ActivityInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.ViewAssertion
import android.view.View

@RunWith(AndroidJUnit4::class)
class ZoomLandscapeTest {
    @Test
    fun landscapeOrientation_showsVerticalSlider() {
        val scenario = ActivityScenario.launch(BinLocatorActivity::class.java)
        scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        onView(withId(R.id.zoomSlider)).check(matches(isDisplayed()))
        onView(withId(R.id.zoomSlider)).check(hasRotation(-90f))
    }

    private fun hasRotation(expected: Float): ViewAssertion = ViewAssertion { view, _ ->
        assertEquals(expected, view.rotation, 0.5f)
    }
}
