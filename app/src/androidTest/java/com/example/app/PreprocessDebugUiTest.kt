package com.example.app

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreprocessDebugUiTest {
    @Test
    fun capture_showsProcessedImage() {
        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, PreprocessDebugActivity::class.java)
        ActivityScenario.launch<PreprocessDebugActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val method = PreprocessDebugActivity::class.java.getDeclaredMethod("takePhoto")
                method.isAccessible = true
                method.invoke(activity)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            onView(withId(R.id.processedImage)).check(matches(isDisplayed()))
        }
    }
}
