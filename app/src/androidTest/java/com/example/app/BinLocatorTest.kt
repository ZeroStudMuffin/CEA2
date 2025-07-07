package com.example.app

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BinLocatorTest {
    @Test
    fun launchActivity_showsCaptureButton() {
        ActivityScenario.launch(BinLocatorActivity::class.java)
        onView(withId(R.id.captureButton)).check(matches(isDisplayed()))
    }
}
