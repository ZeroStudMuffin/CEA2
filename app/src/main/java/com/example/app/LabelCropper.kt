package com.example.app

import android.graphics.Bitmap
import android.graphics.Color

/**
 * Utility for refining a cropped bitmap to the label region.
 */
object LabelCropper {
    private const val EXPECTED_RATIO = 34f / 15f
    private const val TOLERANCE = 0.15f

    /**
     * Returns a grayscale bitmap cropped to the expected label area.
     *
     * The image is first center-cropped to match the overlay aspect ratio,
     * then white borders are trimmed and the result converted to grayscale.
     */
    fun refineCrop(src: Bitmap): Bitmap {
        val ratio = src.width.toFloat() / src.height.toFloat()
        val diff = kotlin.math.abs(ratio - EXPECTED_RATIO) / EXPECTED_RATIO
        val centerCropped = if (diff <= TOLERANCE) {
            src
        } else if (ratio > EXPECTED_RATIO) {
            val targetWidth = (src.height * EXPECTED_RATIO).toInt()
            val left = (src.width - targetWidth) / 2
            Bitmap.createBitmap(src, left, 0, targetWidth, src.height)
        } else {
            val targetHeight = (src.width / EXPECTED_RATIO).toInt()
            val top = (src.height - targetHeight) / 2
            Bitmap.createBitmap(src, 0, top, src.width, targetHeight)
        }

        val trimmed = trimWhiteBorders(centerCropped)
        val gray = ImageUtils.toGrayscale(trimmed)
        return gray
    }

    private fun trimWhiteBorders(bitmap: Bitmap): Bitmap {
        var left = 0
        var right = bitmap.width - 1
        var top = 0
        var bottom = bitmap.height - 1

        fun isWhite(pixel: Int): Boolean {
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            return r > 240 && g > 240 && b > 240
        }

        fun columnHasContent(x: Int): Boolean {
            for (y in 0 until bitmap.height) {
                if (!isWhite(bitmap.getPixel(x, y))) return true
            }
            return false
        }

        fun rowHasContent(y: Int): Boolean {
            for (x in 0 until bitmap.width) {
                if (!isWhite(bitmap.getPixel(x, y))) return true
            }
            return false
        }

        while (left < right && !columnHasContent(left)) left++
        while (right > left && !columnHasContent(right)) right--
        while (top < bottom && !rowHasContent(top)) top++
        while (bottom > top && !rowHasContent(bottom)) bottom--

        return Bitmap.createBitmap(bitmap, left, top, right - left + 1, bottom - top + 1)
    }
}

