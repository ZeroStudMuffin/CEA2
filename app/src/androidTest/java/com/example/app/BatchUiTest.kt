package com.example.app

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BatchUiTest {
    @Test
    fun batchMode_showsAddItemButton() {
        ActivityScenario.launch(BinLocatorActivity::class.java).use {
            onView(withId(R.id.addItemButton)).check(matches(isDisplayed()))
        }
    }
}
