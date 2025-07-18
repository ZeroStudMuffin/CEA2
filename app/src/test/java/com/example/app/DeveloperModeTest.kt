package com.example.app

import android.content.Intent
import android.view.View
import android.widget.Button
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowApplication

@RunWith(RobolectricTestRunner::class)
class DeveloperModeTest {
    @Ignore("Robolectric dependencies not available in CI")
    @Test
    fun developerPin_showsButton() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        // allow PIN manually
        MainActivity::class.java.getDeclaredField("allowedPins").apply { isAccessible = true }.set(activity, setOf("8789"))
        activity.onPinEntered("8789")
        val button = activity.findViewById<Button>(R.id.developerButton)
        assertEquals(View.VISIBLE, button.visibility)
    }

    @Ignore("Robolectric dependencies not available in CI")
    @Test
    fun debugFlag_passedToBinLocator() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        MainActivity::class.java.getDeclaredField("debugFlag").apply { isAccessible = true }.set(activity, true)
        val binButton = activity.findViewById<Button>(R.id.binLocatorButton)
        binButton.performClick()
        val intent = ShadowApplication.getInstance().nextStartedActivity
        assertEquals(true, intent.getBooleanExtra("debug", false))
    }
}
