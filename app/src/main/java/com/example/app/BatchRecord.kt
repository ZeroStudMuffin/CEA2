package com.example.app

/**
 * Simple data holder for queued batch records.
 */
data class BatchRecord(
    var roll: String,
    var customer: String,
    var bin: String? = null
)
