package com.example.app

import android.graphics.Bitmap
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LabelCropperTest {
    @Test
    @org.junit.Ignore("OpenCV not available in CI")
    fun cropLabel_returnsBitmap() {
        val bmp = Bitmap.createBitmap(100, 50, Bitmap.Config.ARGB_8888)
        val result = LabelCropper.cropLabel(bmp)
        assertTrue(result.width > 0 && result.height > 0)
    }
}
