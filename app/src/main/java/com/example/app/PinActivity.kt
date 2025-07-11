package com.example.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class PinActivity : AppCompatActivity() {
    private lateinit var pinEdit: EditText
    private lateinit var submitButton: Button
    private lateinit var progress: ProgressBar
    private var validPins: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        pinEdit = findViewById(R.id.pinEdit)
        submitButton = findViewById(R.id.pinSubmit)
        progress = findViewById(R.id.pinProgress)

        submitButton.setOnClickListener {
            val entered = pinEdit.text.toString()
            if (entered in validPins) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Snackbar.make(pinEdit, "Invalid PIN", Snackbar.LENGTH_SHORT).show()
            }
        }

        fetchPins()
    }

    private fun fetchPins() {
        progress.visibility = View.VISIBLE
        submitButton.isEnabled = false
        PinRepository.fetchPins { pins, error ->
            runOnUiThread {
                progress.visibility = View.GONE
                submitButton.isEnabled = true
                if (error != null) {
                    Snackbar.make(pinEdit, "Failed to load pins", Snackbar.LENGTH_LONG).show()
                }
                validPins = pins
            }
        }
    }
}

