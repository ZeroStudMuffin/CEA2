package com.example.app

/** Utility functions for camera zoom. */
object ZoomUtils {
    /**
     * Ensures the zoom ratio is at least 1x.
     */
    fun clampZoomRatio(ratio: Float): Float = if (ratio < 1f) 1f else ratio

    /**
     * Clamps a linear zoom value to the valid range of 0–1.
     */
    fun clampLinearZoom(value: Float): Float =
        when {
            value < 0f -> 0f
            value > 1f -> 1f
            else -> value
        }
}
