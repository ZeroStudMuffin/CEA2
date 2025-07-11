package com.example.app

import org.junit.Test
import org.junit.Assert.assertEquals

class PinRepositoryTest {
    @Test
    fun parsePins_extractsPins() {
        val csv = "Pins,Assigned\n1111,A\n2222,B\n"
        val expected = setOf("1111", "2222")
        assertEquals(expected, PinRepository.parsePins(csv))
    }

    @Test
    fun parsePins_ignoresEmptyLines() {
        val csv = "Pins,Assigned\n\n3333,C\n"
        val expected = setOf("3333")
        assertEquals(expected, PinRepository.parsePins(csv))
    }
}

