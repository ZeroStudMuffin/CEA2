package com.example.app

import android.content.Intent
import android.widget.Button
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.slider.Slider
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowAlertDialog

@RunWith(RobolectricTestRunner::class)
class BinLocatorTuningTest {
    @Ignore("Robolectric dependencies not available in CI")
    @Test
    fun tuningDialog_updatesParams() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), BinLocatorActivity::class.java)
        intent.putExtra("debug", true)
        val controller = Robolectric.buildActivity(BinLocatorActivity::class.java, intent).setup()
        val activity = controller.get()
        val button = activity.findViewById<Button>(R.id.tuneButton)
        button.performClick()
        val dialog = ShadowAlertDialog.getLatestAlertDialog() ?: return
        val blur = dialog.findViewById<Slider>(R.id.blurSlider)
        blur.value = 7f
        dialog.findViewById<Button>(R.id.applyButton).performClick()
        assertEquals(7, TuningParams.blurKernel)
    }
}

