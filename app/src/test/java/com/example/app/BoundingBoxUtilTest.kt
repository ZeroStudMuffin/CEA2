package com.example.app

import android.graphics.RectF
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BoundingBoxUtilTest {

    @Test
    fun calculateRect_maintainsAspectRatio() {
        val rect = BoundingBoxOverlay.calculateBoxRect(1000, 800)
        val ratio = rect.width() / rect.height()
        val expected = 34f / 15f
        assertEquals(expected, ratio, 0.01f)
    }

    @Test
    fun scaleRect_mapsToBitmapSize() {
        val viewRect = RectF(100f, 100f, 500f, 250f)
        val result = BoundingBoxOverlay.scaleRect(viewRect, 1000, 800, 2000, 1600)
        assertEquals(200, result.left)
        assertEquals(200, result.top)
        assertEquals(1000, result.right)
        assertEquals(500, result.bottom)
    }
}
