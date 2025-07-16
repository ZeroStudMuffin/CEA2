package com.example.app

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ImageUtilsTest {
    @Test
    @org.junit.Ignore("Robolectric dependencies not available in CI")
    fun toGrayscale_convertsColor() {
        val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        bmp.setPixel(0, 0, 0xFF00FF00.toInt())
        val gray = ImageUtils.toGrayscale(bmp)
        val pixel = gray.getPixel(0, 0)
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        assertEquals(r, g)
        assertEquals(g, b)
    }
}
