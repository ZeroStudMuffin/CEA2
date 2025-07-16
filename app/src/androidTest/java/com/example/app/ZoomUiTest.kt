package com.example.app

import androidx.camera.view.LifecycleCameraController
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
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.UiController
import android.view.View

@RunWith(AndroidJUnit4::class)
class ZoomUiTest {
    @Test
    fun launchActivity_showsZoomControls() {
        ActivityScenario.launch(BinLocatorActivity::class.java)
        onView(withId(R.id.zoomSlider)).check(matches(isDisplayed()))
    }

    @Test
    fun defaultZoom_isFortyPercent() {
        val scenario = ActivityScenario.launch(BinLocatorActivity::class.java)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        scenario.onActivity { activity ->
            val field = BinLocatorActivity::class.java.getDeclaredField("controller")
            field.isAccessible = true
            val controller = field.get(activity) as LifecycleCameraController
            val zoom = controller.zoomState.value?.linearZoom ?: 0f
            assertEquals(0.4f, zoom, 0.05f)
        }
    }

    @Test
    fun sliderChange_updatesZoomState() {
        val scenario = ActivityScenario.launch(BinLocatorActivity::class.java)
        onView(withId(R.id.zoomSlider)).perform(setSliderValue(0.5f))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        scenario.onActivity { activity ->
            val field = BinLocatorActivity::class.java.getDeclaredField("controller")
            field.isAccessible = true
            val controller = field.get(activity) as LifecycleCameraController
            val zoom = controller.zoomState.value?.linearZoom ?: 0f
            assertEquals(0.5f, zoom, 0.05f)
        }
    }


    private fun setSliderValue(value: Float): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> = isAssignableFrom(Slider::class.java)

        override fun getDescription(): String = "Set slider value"

        override fun perform(uiController: UiController?, view: View?) {
            (view as Slider).value = value
        }
    }
}
