package com.example.app

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Ignore
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Ignore("Robolectric dependencies not available in CI")
class BatchRecordTest {
    @Test
    fun record_holdsValues() {
        val r = BatchRecord("1", "Cust")
        assertEquals("1", r.roll)
        assertEquals("Cust", r.customer)
        assertEquals(null, r.bin)
    }

    @Ignore("Robolectric dependencies not available in CI")
    @Test
    fun applyBin_updatesAll() {
        val items = mutableListOf(BatchRecord("1", "A"), BatchRecord("2", "B"))
        items.forEach { it.bin = null }
        for (item in items) item.bin = "19"
        assertEquals(listOf("19", "19"), items.map { it.bin })
    }
}
