package com.example.app

import android.graphics.Bitmap

/**
 * Utility for refining a cropped bitmap to the label region.
 */
object LabelCropper {
    private const val EXPECTED_RATIO = 34f / 15f
    private const val TOLERANCE = 0.15f

    /**
     * Returns a bitmap cropped to roughly the expected label aspect ratio.
     *
     * If the input already matches the ratio within 15%, the original bitmap is
     * returned. Otherwise the image is cropped centrally to achieve the ratio.
     */
    fun refineCrop(src: Bitmap): Bitmap {
        val ratio = src.width.toFloat() / src.height.toFloat()
        val diff = kotlin.math.abs(ratio - EXPECTED_RATIO) / EXPECTED_RATIO
        if (diff <= TOLERANCE) return src
        return if (ratio > EXPECTED_RATIO) {
            val targetWidth = (src.height * EXPECTED_RATIO).toInt()
            val left = (src.width - targetWidth) / 2
            Bitmap.createBitmap(src, left, 0, targetWidth, src.height)
        } else {
            val targetHeight = (src.width / EXPECTED_RATIO).toInt()
            val top = (src.height - targetHeight) / 2
            Bitmap.createBitmap(src, 0, top, src.width, targetHeight)
        }
    }
}

