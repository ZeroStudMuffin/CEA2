package com.example.app

import android.widget.Button
import android.widget.CheckBox
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowApplication

@RunWith(RobolectricTestRunner::class)
class MainActivityUnitTest {
    @Ignore("Robolectric dependencies not available in CI")
    @Test
    fun clickingButton_passesBatchExtra() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        val batch = activity.findViewById<CheckBox>(R.id.batchCheckBox)
        batch.isChecked = true
        val button = activity.findViewById<Button>(R.id.binLocatorButton)
        button.performClick()
        val intent = ShadowApplication.getInstance().nextStartedActivity
        assertEquals(true, intent.getBooleanExtra("batch", false))
    }
}
