package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity

/** Activity showing developer options. */
class DeveloperActivity : AppCompatActivity() {
    private lateinit var debugCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer)
        debugCheckBox = findViewById(R.id.debugCheckBox)
        debugCheckBox.isChecked = intent.getBooleanExtra("debug", false)
        findViewById<Button>(R.id.preprocessButton).setOnClickListener {
            startActivity(Intent(this, PreprocessDebugActivity::class.java))
        }
        findViewById<Button>(R.id.livePreviewButton).setOnClickListener {
            startActivity(Intent(this, LiveEdgePreviewActivity::class.java))
        }
    }

    override fun finish() {
        val data = Intent().putExtra("debug", debugCheckBox.isChecked)
        setResult(RESULT_OK, data)
        super.finish()
    }
}
