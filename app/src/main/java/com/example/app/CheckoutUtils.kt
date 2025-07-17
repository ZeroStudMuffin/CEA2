package com.example.app

/** Utility functions for checkout logic. */
object CheckoutUtils {
    /**
     * Builds the final list of checkout records including the current lines and queued items.
     */
    fun buildPayload(lines: List<String>, queued: List<BatchRecord>): List<BatchRecord> {
        val roll = lines.firstOrNull { it.startsWith("Roll#:") }?.substringAfter(":")?.trim()
        val cust = lines.firstOrNull { it.startsWith("Cust:") }?.substringAfter(":")?.trim()
        val result = mutableListOf<BatchRecord>()
        if (roll != null && cust != null) {
            result += BatchRecord(roll, cust, null)
        }
        result.addAll(queued)
        return result
    }
}
