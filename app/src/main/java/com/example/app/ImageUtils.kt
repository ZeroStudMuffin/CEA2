package com.example.app

import android.graphics.Bitmap
import android.graphics.Matrix

/** Utility functions for image manipulation. */
object ImageUtils {
    /**
     * Rotate a bitmap by the given degrees.
     */
    fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees % 360 == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
