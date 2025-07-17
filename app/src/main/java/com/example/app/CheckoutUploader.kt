package com.example.app

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Utility for uploading checkout requests to checkout.php.
 */
object CheckoutUploader {
    private const val TAG = "CheckoutUploader"
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    /** Allows tests to provide a mock connection. */
    var connectionFactory: (URL) -> HttpURLConnection = { url ->
        url.openConnection() as HttpURLConnection
    }

    /**
     * Sends the given batch items and PIN to the server.
     *
     * @param items records to checkout
     * @param pin user PIN
     * @param onComplete callback with success flag and optional message
     */
    fun checkoutItems(
        items: List<BatchRecord>,
        pin: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        executor.execute {
            try {
                val url = URL("https://unitedexpresstrucking.com/checkout.php")
                val conn = connectionFactory(url).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded"
                    )
                }

                val body = buildString {
                    items.forEach { item ->
                        Log.d(TAG, "roll_num=${item.roll}, customer=${item.customer}, last_user=$pin")
                        append("roll_num[]=")
                        append(URLEncoder.encode(item.roll, "UTF-8"))
                        append("&customer[]=")
                        append(URLEncoder.encode(item.customer, "UTF-8"))
                        append('&')
                    }
                    append("last_user=")
                    append(URLEncoder.encode(pin, "UTF-8"))
                }
                conn.outputStream.use { it.write(body.toByteArray()) }

                val code = conn.responseCode
                val stream = if (code >= 400) conn.errorStream else conn.inputStream
                val text = stream?.bufferedReader()?.readText() ?: ""
                stream?.close()
                conn.disconnect()

                val json = try { JSONObject(text) } catch (_: Exception) {
                    JSONObject().put("status", "error").put("message", "Invalid response")
                }
                val success = code == 200 && json.optString("status") != "error"
                onComplete(success, json.optString("message"))
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }
}
