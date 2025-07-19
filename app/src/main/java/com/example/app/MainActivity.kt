package com.example.app

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binButton: Button
    private lateinit var checkoutButton: Button
    private lateinit var developerButton: Button
    private var allowedPins: Set<String> = emptySet()
    private var pin: String = ""
    private var developerMode: Boolean = false
    private var debugFlag: Boolean = false
    private lateinit var devLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val batchCheckBox = findViewById<CheckBox>(R.id.batchCheckBox)
        developerButton = findViewById(R.id.developerButton)
        developerButton.setOnClickListener {
            val intent = Intent(this, DeveloperActivity::class.java)
            intent.putExtra("debug", debugFlag)
            devLauncher.launch(intent)
        }
        devLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                debugFlag = result.data?.getBooleanExtra("debug", false) ?: false
            }
        }
        binButton = findViewById(R.id.binLocatorButton)
        checkoutButton = findViewById(R.id.checkoutButton)
        binButton.isEnabled = false
        checkoutButton.isEnabled = false
        binButton.setOnClickListener {
            val intent = Intent(this, BinLocatorActivity::class.java)
            intent.putExtra("debug", debugFlag)
            intent.putExtra("batch", batchCheckBox.isChecked)
            intent.putExtra("pin", pin)
            startActivity(intent)
        }
        checkoutButton.setOnClickListener {
            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putExtra("pin", pin)
            intent.putExtra("batch", true)
            intent.putExtra("debug", debugFlag)
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
                onPinEntered(input.text.toString().trim())
            }
            .setNegativeButton("Exit") { _, _ -> finish() }
            .show()
    }

    internal fun onPinEntered(entered: String) {
        if (entered.length == 4 && allowedPins.contains(entered)) {
            pin = entered
            binButton.isEnabled = true
            checkoutButton.isEnabled = true
            if (entered == "8789") {
                developerMode = true
                developerButton.visibility = View.VISIBLE
            }
        } else {
            Toast.makeText(this, "Invalid PIN", Toast.LENGTH_SHORT).show()
            showPinDialog()
        }
    }
}
