package com.example.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutOrientationTest {
    @Test
    fun landscapeLayout_hasVerticalSlider() {
        val path = "src/main/res/layout-land/activity_bin_locator.xml"
        val text = File(path).readText()
        assertTrue(text.contains("orientation=\"vertical\""))
    }
}
