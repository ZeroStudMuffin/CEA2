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
    fun extractRelease_returnsFirstRawValue() {
        val codes = listOf(fakeBarcode("abc"), fakeBarcode("def"))
        assertEquals("abc", BarcodeUtils.extractRelease(codes))
    }

    @Test
    fun extractRelease_returnsNullWhenEmpty() {
        assertNull(BarcodeUtils.extractRelease(emptyList()))
    }

    @Test
    fun extractBin_returnsFirstRawValue() {
        val codes = listOf(fakeBarcode("BIN:42"))
        assertEquals("BIN:42", BarcodeUtils.extractBin(codes))
    }
}
