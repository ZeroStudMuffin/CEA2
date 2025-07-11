package com.example.app

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DebugUiTest {
    @Test
    fun debugMode_showsButtonsAndHidesSend() {
        val intent = Intent( androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext, BinLocatorActivity::class.java )
        intent.putExtra("debug", true)
        ActivityScenario.launch<BinLocatorActivity>(intent).use {
            onView(withId(R.id.showOcrButton)).check(matches(isDisplayed()))
            onView(withId(R.id.showCropButton)).check(matches(isDisplayed()))
            onView(withId(R.id.sendRecordButton)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        }
    }
}
