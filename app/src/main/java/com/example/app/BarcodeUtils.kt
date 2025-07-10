package com.example.app

import com.google.mlkit.vision.barcode.common.Barcode

/** Utility functions for returning raw barcode values. */
object BarcodeUtils {
    /**
     * Returns the raw value of the first barcode, or `null` if none exist.
     */
    fun extractRelease(list: List<Barcode>): String? =
        list.firstOrNull()?.rawValue

    /**
     * Returns the raw value of the first barcode, or `null` if none exist.
     */
    fun extractBin(list: List<Barcode>): String? =
        list.firstOrNull()?.rawValue
}
