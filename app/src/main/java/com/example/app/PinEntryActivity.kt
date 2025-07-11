package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class PinEntryActivity : AppCompatActivity() {
    private lateinit var pinEditText: EditText
    private lateinit var submitButton: Button
    private var pins: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_entry)
        pinEditText = findViewById(R.id.pinEditText)
        submitButton = findViewById(R.id.submitPinButton)

        submitButton.setOnClickListener { validatePin() }
        loadPins()
    }

    private fun loadPins() {
        PinFetcher.fetchPins { list ->
            pins = list
        }
    }

    private fun validatePin() {
        val input = pinEditText.text.toString().trim()
        if (input.length != 4) {
            Snackbar.make(pinEditText, "Enter 4 digit PIN", Snackbar.LENGTH_SHORT).show()
            return
        }
        if (pins.contains(input)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            Snackbar.make(pinEditText, "Invalid PIN", Snackbar.LENGTH_SHORT).show()
        }
    }
}
