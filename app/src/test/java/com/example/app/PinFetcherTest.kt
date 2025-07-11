package com.example.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.HttpURLConnection
import java.net.URL

@RunWith(RobolectricTestRunner::class)
class PinFetcherTest {
    @Test
    fun parsePins_returnsFirstColumn() {
        val csv = "Pins,Name\n1234,Bob\n4567,Alice"
        val result = PinFetcher.parsePins(csv)
        assertEquals(listOf("1234", "4567"), result)
    }

    @Test
    fun fetchPins_returnsParsedPins() {
        val csv = "Pins,Name\n1111,Bob"
        val conn = object : HttpURLConnection(URL("http://test")) {
            override fun getInputStream() = csv.byteInputStream()
            override fun getResponseCode() = 200
            override fun connect() {}
            override fun disconnect() {}
            override fun usingProxy() = false
            override fun getOutputStream() = java.io.ByteArrayOutputStream()
        }
        PinFetcher.connectionFactory = { conn }
        var result: List<String> = emptyList()
        PinFetcher.fetchPins { list -> result = list }
        Thread.sleep(100)
        assertEquals(listOf("1111"), result)
    }

    @Test
    fun fetchPins_handlesFailure() {
        PinFetcher.connectionFactory = { throw RuntimeException("bad") }
        var result: List<String> = listOf("x")
        PinFetcher.fetchPins { list -> result = list }
        Thread.sleep(100)
        assertTrue(result.isEmpty())
    }
}
