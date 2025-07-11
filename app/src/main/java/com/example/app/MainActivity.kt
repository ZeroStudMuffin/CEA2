package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val debugCheckBox = findViewById<CheckBox>(R.id.debugCheckBox)
        findViewById<Button>(R.id.binLocatorButton).setOnClickListener {
            val intent = Intent(this, BinLocatorActivity::class.java)
            intent.putExtra("debug", debugCheckBox.isChecked)
            startActivity(intent)
        }
    }
}
