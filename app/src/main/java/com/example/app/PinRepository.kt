package com.example.app

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object PinRepository {
    private const val CSV_URL = "https://docs.google.com/spreadsheets/d/1Xok6zJcUC_bizTy303VFCZHkrFBtW1IxeuT4PIw7Ngo/export?format=csv"

    fun fetchPins(callback: (Set<String>, Exception?) -> Unit) {
        Thread {
            try {
                val url = URL(CSV_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                val csv = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                val pins = parsePins(csv)
                callback(pins, null)
            } catch (e: Exception) {
                callback(emptySet(), e)
            }
        }.start()
    }

    fun parsePins(csv: String): Set<String> {
        return csv.lineSequence()
            .drop(1)
            .map { line -> line.split(',').firstOrNull()?.trim() ?: "" }
            .filter { it.isNotEmpty() }
            .toSet()
    }
}

