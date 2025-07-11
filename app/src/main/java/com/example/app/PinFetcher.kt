package com.example.app

import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/** Utility for fetching and parsing allowed PINs from a Google Sheet. */
object PinFetcher {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private const val SHEET_URL =
        "https://docs.google.com/spreadsheets/d/1Xok6zJcUC_bizTy303VFCZHkrFBtW1IxeuT4PIw7Ngo/export?format=csv&gid=0"

    /** Parses the first column of the provided CSV text and returns unique PINs. */
    fun parseCsv(csv: String): Set<String> {
        return csv.lines()
            .drop(1) // remove header
            .mapNotNull { line -> line.split(',').firstOrNull()?.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    /**
     * Fetches the allowed PINs from the Google Sheet.
     *
     * @param onSuccess called with the fetched PIN set.
     * @param onError called if the fetch fails.
     */
    fun fetchPins(onSuccess: (Set<String>) -> Unit, onError: (Exception) -> Unit) {
        executor.execute {
            try {
                val url = URL(SHEET_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.connect()
                val csv = conn.inputStream.bufferedReader().readText()
                conn.disconnect()
                val pins = parseCsv(csv)
                onSuccess(pins)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}
