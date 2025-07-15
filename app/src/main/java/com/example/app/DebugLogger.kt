package com.example.app

import android.util.Log

/** Simple logger storing debug messages in memory. */
object DebugLogger {
    private val logs = mutableListOf<String>()

    fun log(message: String) {
        logs += message
        Log.d("DebugLogger", message)
    }

    fun getLogs(): List<String> = logs.toList()

    fun clear() {
        logs.clear()
    }
}
