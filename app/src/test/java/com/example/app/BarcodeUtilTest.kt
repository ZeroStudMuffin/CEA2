package com.example.app

import com.google.mlkit.vision.barcode.common.Barcode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito

class BarcodeUtilTest {
    private fun fakeBarcode(value: String?): Barcode {
        val barcode = Mockito.mock(Barcode::class.java)
        Mockito.`when`(barcode.rawValue).thenReturn(value)
        return barcode
    }

    @Test
    fun extractRelease_findsSevenDigits() {
        val codes = listOf(fakeBarcode("foo"), fakeBarcode("1234567"))
        assertEquals("1234567", BarcodeUtils.extractRelease(codes))
    }

    @Test
    fun extractRelease_returnsNullWhenMissing() {
        val codes = listOf(fakeBarcode("foo"))
        assertNull(BarcodeUtils.extractRelease(codes))
    }

    @Test
    fun extractBin_parsesBinPattern() {
        val codes = listOf(fakeBarcode("BIN:42 UNTIED EXPRESS"))
        assertEquals("42", BarcodeUtils.extractBin(codes))
    }
}
