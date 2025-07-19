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

    /**
     * Cleans OCR lines and extracts the roll number and customer name.
     *
     * Lines shorter than 60% of the tallest line are ignored. Spaces become
     * underscores, quoted or bracketed text is removed and only the characters
     * `[A-Za-z0-9_%-/]` are kept. Any prefix before the first underscore is
     * dropped. The line with the most digits is treated as the roll number. The
     * tallest line containing a known keyword (or the line with the most
     * letters when none match) becomes the customer. If the roll contains an
     * underscore, the portion before the last underscore is removed.
     */
    fun parse(lines: List<Text.Line>): List<String> {
        if (lines.isEmpty()) return emptyList()

        val tallest = lines.maxOfOrNull { it.boundingBox?.height() ?: 0 } ?: 0
        if (tallest == 0) return emptyList()

        val threshold = (tallest * TuningParams.lineHeightPercent).toInt()

        val quoteRegex = Regex("[\"'].*?[\"']")
        val bracketRegex = Regex("\\[[^\\]]*\\]|\\([^)]*\\)")
        val cleanRegex = Regex("[^A-Za-z0-9_%\-/]")

        data class LineData(val text: String, val height: Int)

        val cleaned = lines.filter { (it.boundingBox?.height() ?: 0) >= threshold }
            .mapNotNull { line ->
                var text = line.text.replace(' ', '_')
                text = text.replace(quoteRegex, "")
                text = text.replace(bracketRegex, "")
                text = text.replace(cleanRegex, "").trim()

                if (text.isEmpty()) null else LineData(text, line.boundingBox?.height() ?: 0)
            }

        if (cleaned.isEmpty()) return emptyList()

        val rollLine = cleaned.maxByOrNull { countDigits(it.text) } ?: cleaned.first()

        val customerLine = cleaned.filter { it.text.containsKnownWord() }
            .maxByOrNull { it.height }
            ?: cleaned.filter { it != rollLine }
                .maxByOrNull { it.text.count { ch -> ch.isLetter() } }
            ?: rollLine

        var roll = rollLine.text
        val lastUnderscore = roll.lastIndexOf('_')
        if (lastUnderscore >= 0) {
            roll = roll.substring(lastUnderscore + 1)
        }

        val customer = customerLine.text

        return listOf("Roll#:$roll", "Cust:$customer")
    }
}
