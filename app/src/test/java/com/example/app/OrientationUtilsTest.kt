package com.example.app

import android.content.pm.ActivityInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class OrientationUtilsTest {
    @Test
    fun degreesToScreenOrientation_mapsAllAngles() {
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, OrientationUtils.degreesToScreenOrientation(0))
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, OrientationUtils.degreesToScreenOrientation(90))
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, OrientationUtils.degreesToScreenOrientation(180))
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, OrientationUtils.degreesToScreenOrientation(270))
    }
}
