package com.example.app

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.robolectric.RobolectricTestRunner
import org.junit.Ignore
import java.net.HttpURLConnection
import java.net.URL

@RunWith(RobolectricTestRunner::class)
class RecordUploaderTest {
    @Ignore("Robolectric dependencies not available in CI")
    @Test
    fun sendRecord_successReturnsTrue() {
        val conn = object : HttpURLConnection(URL("http://test")) {
            override fun getResponseCode(): Int = 200
            override fun getInputStream() = "{\"status\":\"inserted\",\"message\":\"ok\"}".byteInputStream()
            override fun connect() {}
            override fun disconnect() {}
            override fun usingProxy() = false
            override fun getOutputStream() = java.io.ByteArrayOutputStream()
        }
        RecordUploader.connectionFactory = { conn }
        var result = false
        var msg: String? = null
        RecordUploader.sendRecord("1", "Cust", "B") { success, message ->
            result = success
            msg = message
        }
        Thread.sleep(100)
        assertTrue(result)
        assertEquals("ok", msg)
    }

    @Ignore("Robolectric dependencies not available in CI")
    @Test
    fun sendRecord_failureReturnsFalse() {
        val conn = object : HttpURLConnection(URL("http://test")) {
            override fun getResponseCode(): Int = 500
            override fun getErrorStream() = "{\"status\":\"error\",\"message\":\"bad\"}".byteInputStream()
            override fun getInputStream() = getErrorStream()
            override fun connect() {}
            override fun disconnect() {}
            override fun usingProxy() = false
            override fun getOutputStream() = java.io.ByteArrayOutputStream()
        }
        RecordUploader.connectionFactory = { conn }
        var result = true
        var msg: String? = null
        RecordUploader.sendRecord("1", "Cust", "B") { success, message ->
            result = success
            msg = message
        }
        Thread.sleep(100)
        assertFalse(result)
        assertEquals("bad", msg)
    }
}
