package com.example.app

import com.google.mlkit.vision.text.Text

/** Utility for filtering and cleaning OCR lines. */
object OcrParser {
    /**
     * Returns cleaned text lines.
     *
     * Lines shorter than 75% of the tallest line are ignored. Special
     * characters are removed except letters, digits, space, '-', and '%'.
     * Underscores are converted to spaces and text within brackets or quotes
     * is stripped.
     */
    fun parse(lines: List<Text.Line>): List<String> {
        if (lines.isEmpty()) return emptyList()
        val tallest = lines.maxOfOrNull { it.boundingBox?.height() ?: 0 } ?: 0
        if (tallest == 0) return emptyList()
        val threshold = tallest * 0.75
        val quoteRegex = Regex("[\"'].*?[\"']")
        val bracketRegex = Regex("\\[[^\\]]*\\]|\\([^)]*\\)")
        val cleanRegex = Regex("""[^A-Za-z0-9 %\-]""")
        return lines.filter { (it.boundingBox?.height() ?: 0) >= threshold }
            .map { line ->
                var text = line.text.replace('_', ' ')
                text = text.replace(quoteRegex, "")
                text = text.replace(bracketRegex, "")
                text = text.replace(cleanRegex, "").trim()
                text
            }
    }
}
