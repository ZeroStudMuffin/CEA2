package com.example.app

import org.junit.Assert.assertEquals
import org.junit.Test

class CheckoutUtilsTest {
    @Test
    fun buildPayload_includesCurrentAndQueued() {
        val lines = listOf("Roll#: 1", "Cust-Name: A")
        val queued = listOf(BatchRecord("2", "B"))
        val payload = CheckoutUtils.buildPayload(lines, queued)
        assertEquals(2, payload.size)
        assertEquals("1", payload[0].roll)
        assertEquals("A", payload[0].customer)
        assertEquals("2", payload[1].roll)
    }

    @Test
    fun buildPayload_missingCurrentReturnsQueued() {
        val lines = listOf("Cust-Name: A")
        val queued = listOf(BatchRecord("2", "B"))
        val payload = CheckoutUtils.buildPayload(lines, queued)
        assertEquals(1, payload.size)
        assertEquals("2", payload[0].roll)
    }
}
