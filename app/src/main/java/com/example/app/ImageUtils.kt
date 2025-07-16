package com.example.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import androidx.exifinterface.media.ExifInterface
import java.io.File

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

    /**
     * Decode an image file applying any EXIF rotation so the returned bitmap is upright.
     */
    fun decodeRotatedBitmap(file: File): Bitmap {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val exif = ExifInterface(file.absolutePath)
        return rotateBitmap(bitmap, exif.rotationDegrees)
    }

    /**
     * Convert a bitmap to grayscale.
     */
    fun toGrayscale(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }
}
