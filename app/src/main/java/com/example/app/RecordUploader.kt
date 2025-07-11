package com.example.app

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.json.JSONObject

object RecordUploader {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    var connectionFactory: (URL) -> HttpURLConnection = { url ->
        url.openConnection() as HttpURLConnection
    }

    fun sendRecord(
        roll: String,
        customer: String,
        bin: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        executor.execute {
            try {
                val url = URL("https://unitedexpresstrucking.com/insert.php")
                val conn = connectionFactory(url).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                }

                val body = "roll_num=" + URLEncoder.encode(roll, "UTF-8") +
                    "&customer=" + URLEncoder.encode(customer, "UTF-8") +
                    "&bin=" + URLEncoder.encode(bin, "UTF-8")
                conn.outputStream.use { it.write(body.toByteArray()) }

                val responseCode = conn.responseCode
                val stream = if (responseCode >= 400) conn.errorStream else conn.inputStream
                val responseText = stream?.bufferedReader()?.readText() ?: ""
                stream?.close()
                conn.disconnect()

                val json = try {
                    JSONObject(responseText)
                } catch (e: Exception) {
                    JSONObject().put("status", "error").put("message", "Invalid response")
                }

                val status = json.optString("status")
                val message = json.optString("message")
                val success = responseCode == 200 && status != "error"

                onComplete(success, message)
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }
}
