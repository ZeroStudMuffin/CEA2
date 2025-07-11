package com.example.app

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
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
            override fun getInputStream() = "{\"status\":\"success\"}".byteInputStream()
            override fun connect() {}
            override fun disconnect() {}
            override fun usingProxy() = false
            override fun getOutputStream() = java.io.ByteArrayOutputStream()
        }
        RecordUploader.connectionFactory = { conn }
        var result = false
        RecordUploader.sendRecord("1", "Cust", "B") { success -> result = success }
        Thread.sleep(100)
        assertTrue(result)
    }

    @Ignore("Robolectric dependencies not available in CI")
    @Test
    fun sendRecord_failureReturnsFalse() {
        val conn = object : HttpURLConnection(URL("http://test")) {
            override fun getResponseCode(): Int = 500
            override fun getInputStream() = "fail".byteInputStream()
            override fun connect() {}
            override fun disconnect() {}
            override fun usingProxy() = false
            override fun getOutputStream() = java.io.ByteArrayOutputStream()
        }
        RecordUploader.connectionFactory = { conn }
        var result = true
        RecordUploader.sendRecord("1", "Cust", "B") { success -> result = success }
        Thread.sleep(100)
        assertFalse(result)
    }
}
