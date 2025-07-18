package com.example.app

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SendRecordUiTest {
    @Test
    fun showResult_enablesSendButton() {
        val scenario = ActivityScenario.launch(BinLocatorActivity::class.java)
        scenario.onActivity { activity ->
            val method = BinLocatorActivity::class.java.getDeclaredMethod("showResult", List::class.java)
            method.isAccessible = true
            method.invoke(activity, listOf("Roll#:1", "Cust-Name:ACME", "BIN=19"))
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        onView(withId(R.id.sendRecordButton)).check(matches(isEnabled()))
    }
}
