package com.example.app

import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object PinFetcher {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    var connectionFactory: (URL) -> HttpURLConnection = { url ->
        url.openConnection() as HttpURLConnection
    }
    private const val CSV_URL = "https://docs.google.com/spreadsheets/d/1Xok6zJcUC_bizTy303VFCZHkrFBtW1IxeuT4PIw7Ngo/export?format=csv"

    fun fetchPins(onComplete: (List<String>) -> Unit) {
        executor.execute {
            try {
                val url = URL(CSV_URL)
                val conn = connectionFactory(url)
                val csv = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()
                onComplete(parsePins(csv))
            } catch (e: Exception) {
                onComplete(emptyList())
            }
        }
    }

    internal fun parsePins(csv: String): List<String> {
        return csv.lines()
            .drop(1)
            .mapNotNull { line ->
                line.split(',').firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
            }
    }
}
