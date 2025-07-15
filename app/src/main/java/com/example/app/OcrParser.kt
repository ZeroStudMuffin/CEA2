package com.example.app

import com.google.mlkit.vision.text.Text

/** Utility for filtering and cleaning OCR lines. */
object OcrParser {
    private val knownCustomerWords = listOf("CUSTOMER", "CUST", "CLIENT")

    private fun String.containsKnownWord(): Boolean {
        return knownCustomerWords.any { contains(it, ignoreCase = true) }
    }

    private fun countDigits(s: String): Int = s.count { it.isDigit() }

    /**
     * Returns cleaned text lines and extracts roll number and customer name.
     *
     * Lines shorter than 75% of the tallest line are ignored. Special
     * characters are removed except letters, digits, spaces, underscores,
     * '-', and '%'. Spaces are converted to underscores and text within
     * brackets or quotes is stripped.
     */
    fun parse(lines: List<Text.Line>): List<String> {
        if (lines.isEmpty()) return emptyList()
        val tallest = lines.maxOfOrNull { it.boundingBox?.height() ?: 0 } ?: 0
        if (tallest == 0) return emptyList()
        val threshold = tallest * 0.75
        val quoteRegex = Regex("[\"'].*?[\"']")
        val bracketRegex = Regex("\\[[^\\]]*\\]|\\([^)]*\\)")
        // allow letters, digits, spaces, underscores, '-', and '%'
        val cleanRegex = Regex("""[^A-Za-z0-9 _%\-]""")

        val cleanLines = lines.filter { (it.boundingBox?.height() ?: 0) >= threshold }
            .map { line ->
                var text = line.text
                text = text.replace(quoteRegex, "")
                text = text.replace(bracketRegex, "")
                text = text.replace(cleanRegex, "").trim()
                // convert spaces to underscores
                text = text.replace(' ', '_')
                text
            }

        if (cleanLines.isEmpty()) return emptyList()

        val customer = cleanLines.firstOrNull { it.containsKnownWord() }
        val roll = if (customer != null) {
            cleanLines.filterNot { it == customer }.maxByOrNull { it.length }
        } else {
            cleanLines.maxByOrNull { countDigits(it) }
        }
        val name = customer ?: cleanLines.filterNot { it == roll }.maxByOrNull { it.length }

        val rollStr = roll?.let { raw ->
            var trimmed = raw
            val spaceIndex = trimmed.indexOf(' ')
            val underscoreIndex = trimmed.indexOf('_')
            val cutIndex = listOf(spaceIndex, underscoreIndex)
                .filter { it >= 0 }
                .minOrNull() ?: -1
            if (cutIndex != -1) {
                trimmed = trimmed.substring(cutIndex + 1)
            }
            trimmed.replace(" ", "").replace("_", "")
        } ?: ""
        val nameStr = name ?: ""
        return listOf("Roll#:$rollStr", "Cust:$nameStr")
    }
}
