package com.example.app

import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object RecordUploader {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    var connectionFactory: (URL) -> HttpURLConnection = { url ->
        url.openConnection() as HttpURLConnection
    }

    fun sendRecord(
        roll: String,
        customer: String,
        bin: String,
        onComplete: (Boolean) -> Unit
    ) {
        executor.execute {
            try {
                val url = URL("https://unitedexpresstrucking.com/insert.php")
                val conn = connectionFactory(url).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                }
                val body = "roll_num=$roll&customer=$customer&bin=$bin".toByteArray()
                conn.outputStream.use { it.write(body) }
                val success = conn.responseCode == 200 &&
                    conn.inputStream.bufferedReader().readText().contains("success")
                conn.inputStream.close()
                conn.disconnect()
                onComplete(success)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
}
