package com.example.app

import com.google.mlkit.vision.text.Text
import com.example.app.TuningParams

/** Utility for filtering and cleaning OCR lines. */
object OcrParser {
    private val knownCustomerWords = listOf("CUSTOMER", "CUST", "CLIENT")

    private fun String.containsKnownWord(): Boolean {
        return knownCustomerWords.any { contains(it, ignoreCase = true) }
    }

    private fun countDigits(s: String): Int = s.count { it.isDigit() }
    private fun countLetters(s: String): Int = s.count { it.isLetter() }

    /**
     * Returns cleaned text lines and extracts roll number and customer name.
     *
     * Lines shorter than 60% of the tallest line are ignored. Spaces are
     * converted to underscores. Quoted text and content in brackets are
     * removed and all characters except letters, digits, underscore, '-', '%'
     * and '/' are stripped.
     */
    fun parse(lines: List<Text.Line>): List<String> {
        if (lines.isEmpty()) return emptyList()
        val tallest = lines.maxOfOrNull { it.boundingBox?.height() ?: 0 } ?: 0
        if (tallest == 0) return emptyList()
        val threshold = if (TuningParams.useLineHeight) {
            tallest * TuningParams.lineHeightPercent
        } else 0f
        val quoteRegex = Regex("[\"'].*?[\"']")
        val bracketRegex = Regex("\\[[^\\]]*\\]|\\([^)]*\\)")
        val cleanRegex = Regex("""[^A-Za-z0-9_%\-/]""")

        val cleanLines = lines
            .map { it.text to (it.boundingBox?.height() ?: 0) }
            .filter { (_, h) -> h >= threshold }
            .map { (text, h) ->
                var t = text.replace(' ', '_')
                t = t.replace(quoteRegex, "")
                t = t.replace(bracketRegex, "")
                t = t.replace(cleanRegex, "").trim()
                t to h
            }
            .filter { it.first.isNotBlank() }

        if (cleanLines.isEmpty()) return emptyList()

        val rollPair = cleanLines.maxByOrNull { countDigits(it.first) }
        val remaining = cleanLines.filterNot { it == rollPair }

        val customerPair = remaining
            .filter { it.first.containsKnownWord() }
            .maxByOrNull { it.second }
            ?: remaining.maxByOrNull { countLetters(it.first) }

        var rollStr = rollPair?.first ?: ""
        if (rollStr.contains('_')) {
            rollStr = rollStr.substringAfterLast('_')
        }
        val nameStr = customerPair?.first ?: ""
        return listOf("Roll#:$rollStr", "Cust:$nameStr")
    }
}
