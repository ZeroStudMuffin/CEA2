package com.example.app

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
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
}
