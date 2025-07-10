package com.example.app

import android.graphics.Rect
import com.google.mlkit.vision.text.Text
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import org.junit.Ignore

@RunWith(RobolectricTestRunner::class)
class OcrParserTest {
    private fun fakeLine(text: String, height: Int): Text.Line {
        val line = Mockito.mock(Text.Line::class.java)
        Mockito.`when`(line.text).thenReturn(text)
        Mockito.`when`(line.boundingBox).thenReturn(Rect(0, 0, 0, height))
        return line
    }

    @Ignore("Robolectric dependencies not available in CI")
    @Test
    fun parse_filtersAndCleans() {
        val lines = listOf(
            fakeLine("HELLO_WORLD", 100),
            fakeLine("[skip] 42%", 80),
            fakeLine("short", 50)
        )
        val result = OcrParser.parse(lines)
        assertEquals(listOf("HELLO WORLD", "42%"), result)
    }

    @Ignore("Robolectric dependencies not available in CI")
    @Test
    fun parse_allLinesFiltered() {
        val lines = listOf(fakeLine("foo", 10))
        assertTrue(OcrParser.parse(lines).isEmpty())
    }

    @Ignore("Robolectric dependencies not available in CI")
    @Test
    fun parse_removesSpecialChars() {
        val lines = listOf(fakeLine("Foo@Bar!", 100))
        assertEquals(listOf("FooBar"), OcrParser.parse(lines))
    }

    @Ignore("Robolectric dependencies not available in CI")
    @Test
    fun parse_extractsCustomerByKnownWord() {
        val lines = listOf(
            fakeLine("CUSTOMER ACME", 100),
            fakeLine("ROLL 42", 100)
        )
        val result = OcrParser.parse(lines)
        assertEquals(
            listOf("Roll#:ROLL 42", "Cust-Name:CUSTOMER ACME"),
            result
        )
    }

    @Ignore("Robolectric dependencies not available in CI")
    @Test
    fun parse_extractsByDigitCount() {
        val lines = listOf(
            fakeLine("AA12", 100),
            fakeLine("98765", 100),
            fakeLine("LONGESTNAME", 100)
        )
        val result = OcrParser.parse(lines)
        assertEquals(
            listOf("Roll#:98765", "Cust-Name:LONGESTNAME"),
            result
        )
    }

    @Ignore("Robolectric dependencies not available in CI")
    @Test
    fun parse_trimsRollPrefix() {
        val lines = listOf(
            fakeLine("ROLL 12345", 100),
            fakeLine("CUSTOMER ACME", 100)
        )
        val result = OcrParser.parse(lines)
        assertEquals(
            listOf("Roll#:12345", "Cust-Name:CUSTOMER ACME"),
            result
        )
    }

    @Ignore("Robolectric dependencies not available in CI")
    @Test
    fun parse_returnsEmptyWhenNoLines() {
        assertTrue(OcrParser.parse(emptyList()).isEmpty())
    }
}
