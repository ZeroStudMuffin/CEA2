package com.example.app

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BinMenuUiTest {
    @Test
    fun nonBatchMode_showsBinOverlay() {
        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, BinLocatorActivity::class.java)
        intent.putExtra("batch", false)
        ActivityScenario.launch<BinLocatorActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val method = BinLocatorActivity::class.java.getDeclaredMethod("showResult", List::class.java)
                method.isAccessible = true
                method.invoke(activity, listOf("Roll#:1", "Cust:Bob"))
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            onView(withId(R.id.binMenuContainer)).check(matches(isDisplayed()))
            onView(withId(R.id.addItemButton)).check(matches(not(isDisplayed())))
        }
    }
}
