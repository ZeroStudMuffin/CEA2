package com.example.app

import org.junit.Assert.assertEquals
import org.junit.Test

class PinFetcherTest {
    @Test
    fun parseCsv_returnsPins() {
        val csv = "Pins,Assigned\n1234,Alice\n5678,Bob"
        val expected = setOf("1234", "5678")
        assertEquals(expected, PinFetcher.parseCsv(csv))
    }

    @Test
    fun parseCsv_handlesEmpty() {
        val csv = "Pins,Assigned\n"
        assertEquals(emptySet<String>(), PinFetcher.parseCsv(csv))
    }
}
