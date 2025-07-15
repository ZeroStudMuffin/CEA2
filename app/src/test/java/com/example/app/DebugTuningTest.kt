package com.example.app

import org.junit.Assert.assertEquals
import org.junit.Test

class DebugTuningTest {
    @Test
    fun reset_restoresDefaults() {
        DebugTuning.options.heightPercent = 0.5f
        DebugTuning.reset()
        assertEquals(0.75f, DebugTuning.options.heightPercent)
    }
}
