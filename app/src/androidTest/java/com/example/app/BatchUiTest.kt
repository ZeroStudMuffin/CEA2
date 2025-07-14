package com.example.app

import android.content.Intent
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
class BatchUiTest {
    @Test
    fun batchMode_showsAddItemButton() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, BinLocatorActivity::class.java).apply {
            putExtra("batch", true)
        }
        ActivityScenario.launch<BinLocatorActivity>(intent).use {
            onView(withId(R.id.addItemButton)).check(matches(isDisplayed()))
        }
    }
}
