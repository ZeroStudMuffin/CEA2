package com.example.app

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import org.junit.Assert.assertEquals
import org.junit.Test

class LabelCropperTest {
    @Test
    fun refineCrop_keepsSizeAndGrayscaleWhenRatioClose() {
        val src = Bitmap.createBitmap(340, 150, Config.ARGB_8888)
        src.eraseColor(Color.RED)
        val result = LabelCropper.refineCrop(src)
        assertEquals(340, result.width)
        assertEquals(150, result.height)
        val pixel = result.getPixel(0, 0)
        val r = Color.red(pixel)
        assertEquals(r, Color.green(pixel))
        assertEquals(r, Color.blue(pixel))
    }

    @Test
    fun refineCrop_trimsWhiteBorders() {
        val src = Bitmap.createBitmap(100, 60, Config.ARGB_8888)
        val canvas = Canvas(src)
        canvas.drawColor(Color.WHITE)
        val paint = Paint().apply { color = Color.BLACK }
        canvas.drawRect(10f, 10f, 90f, 50f, paint)

        val result = LabelCropper.refineCrop(src)

        assertEquals(80, result.width)
        assertEquals(40, result.height)
    }
}

