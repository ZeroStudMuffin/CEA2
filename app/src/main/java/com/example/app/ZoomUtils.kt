package com.example.app

/** Utility functions for camera zoom. */
object ZoomUtils {
    /**
     * Ensures the zoom ratio is at least 1x.
     */
    fun clampZoomRatio(ratio: Float): Float = if (ratio < 1f) 1f else ratio
}
