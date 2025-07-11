package com.example.app

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binButton: Button
    private var allowedPins: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binButton = findViewById(R.id.binLocatorButton)
        binButton.isEnabled = false
        binButton.setOnClickListener {
            startActivity(Intent(this, BinLocatorActivity::class.java))
        }

        PinFetcher.fetchPins({ pins ->
            allowedPins = pins
            runOnUiThread { showPinDialog() }
        }, { e ->
            runOnUiThread {
                Toast.makeText(this, "Failed to load PINs", Toast.LENGTH_LONG).show()
                finish()
            }
        })
    }

    private fun showPinDialog() {
        val input = EditText(this).apply { inputType = InputType.TYPE_CLASS_NUMBER }
        AlertDialog.Builder(this)
            .setTitle("Enter PIN")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                val pin = input.text.toString().trim()
                if (pin.length == 4 && allowedPins.contains(pin)) {
                    binButton.isEnabled = true
                } else {
                    Toast.makeText(this, "Invalid PIN", Toast.LENGTH_SHORT).show()
                    showPinDialog()
                }
            }
            .setNegativeButton("Exit") { _, _ -> finish() }
            .show()
    }
}
