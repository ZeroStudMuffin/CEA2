package com.example.app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShowCropUiTest {
    @Test
    fun showCrop_displaysSavedBitmap() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, BinLocatorActivity::class.java).apply {
            putExtra("debug", true)
        }
        ActivityScenario.launch<BinLocatorActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val file = File(activity.cacheDir, "ocr_debug.png")
                val bmp = Bitmap.createBitmap(2, 2, Config.ARGB_8888)
                FileOutputStream(file).use { out ->
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
            onView(withId(R.id.showCropButton)).perform(click())
            onView(withId(R.id.cropPreview)).check(matches(isDisplayed()))
        }
    }
}
