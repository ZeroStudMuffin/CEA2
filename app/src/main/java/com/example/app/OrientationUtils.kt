package com.example.app

import android.content.pm.ActivityInfo

/** Utility functions for handling orientation degrees. */
object OrientationUtils {
    /**
     * Map rotation degrees to ActivityInfo screen orientation constants.
     */
    fun degreesToScreenOrientation(degrees: Int): Int {
        val normalized = ((degrees % 360) + 360) % 360
        return when (normalized) {
            0 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            90 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }
}
