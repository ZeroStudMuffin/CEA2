package com.example.app

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.slider.Slider
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.ViewAction
import androidx.test.espresso.UiController
import android.view.View
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom

@RunWith(AndroidJUnit4::class)
class LiveEdgePreviewUiTest {
    @Test
    fun launch_showsEdgeView() {
        ActivityScenario.launch(LiveEdgePreviewActivity::class.java)
        onView(withId(R.id.edgeView)).check(matches(isDisplayed()))
    }

    @Test
    fun tuningDialog_updatesParams() {
        val scenario = ActivityScenario.launch(LiveEdgePreviewActivity::class.java)
        onView(withId(R.id.tuneButton)).perform(click())
        onView(withId(R.id.cannyLowSlider)).perform(setSliderValue(80f))
        onView(withId(R.id.applyButton)).perform(click())
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertEquals(80, TuningParams.cannyLow)
        scenario.close()
    }

    private fun setSliderValue(value: Float): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> = isAssignableFrom(Slider::class.java)
        override fun getDescription(): String = "Set slider value"
        override fun perform(uiController: UiController?, view: View?) {
            (view as Slider).value = value
        }
    }
}
