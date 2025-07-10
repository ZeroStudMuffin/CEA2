package com.example.app

import com.google.mlkit.vision.barcode.common.Barcode

/** Utility functions for extracting release numbers and bin codes from barcodes. */
object BarcodeUtils {
    private val releaseRegex = Regex("\\b\\d{7}\\b")
    private val binRegex = Regex("BIN:(\\d+) UNITED EXPRESS")

    /**
     * Returns the first 7-digit release number from the barcode list.
     */
    fun extractRelease(list: List<Barcode>): String? =
        list.mapNotNull { it.rawValue }
            .firstOrNull { releaseRegex.containsMatchIn(it) }
            ?.let { releaseRegex.find(it)?.value }

    /**
     * Returns the bin code from a QR pattern `BIN:<#> UNITED EXPRESS`.
     */
    fun extractBin(list: List<Barcode>): String? =
        list.mapNotNull { it.rawValue }
            .firstOrNull { binRegex.containsMatchIn(it) }
            ?.let { binRegex.find(it)?.groupValues?.get(1) }
}
