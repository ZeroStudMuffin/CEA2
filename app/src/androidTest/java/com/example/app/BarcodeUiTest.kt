package com.example.app

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BarcodeUiTest {
    @Test
    fun showResult_showsTextAndButtons() {
        val scenario = ActivityScenario.launch(BinLocatorActivity::class.java)
        scenario.onActivity { activity ->
            val method = BinLocatorActivity::class.java.getDeclaredMethod("showResult", String::class.java)
            method.isAccessible = true
            method.invoke(activity, "test")
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        onView(withId(R.id.actionButtons)).check(matches(isDisplayed()))
        onView(withId(R.id.ocrTextView)).check(matches(withText("test")))
    }
}
