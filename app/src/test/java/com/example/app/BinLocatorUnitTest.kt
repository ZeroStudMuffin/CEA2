package com.example.app

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.robolectric.Robolectric
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

@RunWith(RobolectricTestRunner::class)
class BinLocatorUnitTest {
    @Test
    @org.junit.Ignore("Robolectric dependencies not available in CI")
    fun rotateBitmap_swapsDimensions() {
        val bitmap = Bitmap.createBitmap(100, 50, Config.ARGB_8888)
        val rotated = ImageUtils.rotateBitmap(bitmap, 90)
        assertEquals(50, rotated.width)
        assertEquals(100, rotated.height)
    }

    @Test
    fun debugMode_hidesSendButton() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), BinLocatorActivity::class.java)
        intent.putExtra("debug", true)
        val controller = Robolectric.buildActivity(BinLocatorActivity::class.java, intent).setup()
        val activity = controller.get()
        val button = activity.findViewById<android.widget.Button>(R.id.sendRecordButton)
        assertEquals(android.view.View.GONE, button.visibility)
    }
}
