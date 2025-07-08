package com.example.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ZoomUtilTest {
    @Test
    fun clampZoomRatio_returnsAtLeastOne() {
        assertEquals(1f, ZoomUtils.clampZoomRatio(0.5f))
        assertEquals(2f, ZoomUtils.clampZoomRatio(2f))
    }

    @Test
    fun clampLinearZoom_coercesToRange() {
        assertEquals(0f, ZoomUtils.clampLinearZoom(-0.1f))
        assertEquals(1f, ZoomUtils.clampLinearZoom(1.2f))
        assertEquals(0.5f, ZoomUtils.clampLinearZoom(0.5f))
    }
}
