package com.example.app

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binButton: Button
    private lateinit var checkoutButton: Button
    private var allowedPins: Set<String> = emptySet()
    private var pin: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val batchCheckBox = findViewById<CheckBox>(R.id.batchCheckBox)
        val debugCheckBox = findViewById<CheckBox>(R.id.debugCheckBox)
        binButton = findViewById(R.id.binLocatorButton)
        checkoutButton = findViewById(R.id.checkoutButton)
        binButton.isEnabled = false
        checkoutButton.isEnabled = false
        binButton.setOnClickListener {
            val intent = Intent(this, BinLocatorActivity::class.java)
            intent.putExtra("debug", debugCheckBox.isChecked)
            intent.putExtra("batch", batchCheckBox.isChecked)
            intent.putExtra("pin", pin)
            startActivity(intent)
        }
        checkoutButton.setOnClickListener {
            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putExtra("pin", pin)
            intent.putExtra("batch", true)
            intent.putExtra("debug", debugCheckBox.isChecked)
            startActivity(intent)
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
                val entered = input.text.toString().trim()
                if (entered.length == 4 && allowedPins.contains(entered)) {
                    pin = entered
                    binButton.isEnabled = true
                    checkoutButton.isEnabled = true
                } else {
                    Toast.makeText(this, "Invalid PIN", Toast.LENGTH_SHORT).show()
                    showPinDialog()
                }
            }
            .setNegativeButton("Exit") { _, _ -> finish() }
            .show()
    }
}
