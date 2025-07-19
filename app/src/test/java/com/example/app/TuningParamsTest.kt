package com.example.app

import org.junit.Assert.assertEquals
import org.junit.Test

class TuningParamsTest {
    @Test
    fun defaults_matchGuide() {
        assertEquals(5, TuningParams.blurKernel)
        assertEquals(50, TuningParams.cannyLow)
        assertEquals(150, TuningParams.cannyHigh)
        assertEquals(3, TuningParams.dilateKernel)
        assertEquals(10.0, TuningParams.epsilon, 0.001)
        assertEquals(0.1f, TuningParams.minAreaRatio)
        assertEquals(0.1f, TuningParams.ratioTolerance)
        assertEquals(8.5f / 3.625f, TuningParams.targetRatio, 0.001f)
        assertEquals(800, TuningParams.outputWidth)
        assertEquals(200, TuningParams.outputHeight)
        assertEquals(0.60f, TuningParams.lineHeightPercent)
    }
}

