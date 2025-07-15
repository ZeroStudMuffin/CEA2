package com.example.app

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import org.junit.Assert.assertEquals
import org.junit.Test

class LabelCropperTest {
    @Test
    fun refineCrop_keepsOriginalWhenRatioClose() {
        val src = Bitmap.createBitmap(340, 150, Config.ARGB_8888)
        val result = LabelCropper.refineCrop(src)
        assertEquals(340, result.width)
        assertEquals(150, result.height)
    }

    @Test
    fun refineCrop_cropsToExpectedRatio() {
        val src = Bitmap.createBitmap(300, 200, Config.ARGB_8888)
        val result = LabelCropper.refineCrop(src)
        val expected = 34f / 15f
        val ratio = result.width.toFloat() / result.height
        assertEquals(expected, ratio, 0.1f)
    }
}

