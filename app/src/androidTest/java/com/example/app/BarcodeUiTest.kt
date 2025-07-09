package com.example.app

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BarcodeUiTest {
    @Test
    fun showResult_displaysButtons() {
        val scenario = ActivityScenario.launch(BinLocatorActivity::class.java)
        scenario.onActivity { activity ->
            val method = BinLocatorActivity::class.java.getDeclaredMethod("showResult", String::class.java)
            method.isAccessible = true
            method.invoke(activity, "text")
        }
        onView(withId(R.id.getReleaseButton)).check(matches(isDisplayed()))
        onView(withId(R.id.setBinButton)).check(matches(isDisplayed()))
    }
}
