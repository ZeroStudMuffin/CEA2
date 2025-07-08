package com.example.uetapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    @androidx.camera.core.ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        previewView = findViewById(R.id.viewFinder)
        val captureBtn: Button = findViewById(R.id.btnCapture)
        cameraExecutor = Executors.newSingleThreadExecutor()
        captureBtn.setOnClickListener { takePhoto() }
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Camera initialization failed", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun takePhoto() {
        val capture = imageCapture ?: return
        capture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    processImageProxy(imageProxy)
                }
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@MainActivity, "Capture failed", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun processImageProxy(imageProxy: ImageProxy) {
        val rawBmp = ImageUtils.imageProxyToBitmap(imageProxy)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(rawBmp, 0)
        recognizer.process(image)
            .addOnSuccessListener { text ->
                val (roll, customer) = extractFromText(text)
                runOnUiThread {
                    showResultPopup(roll, customer)
                    saveOcrLog("roll=$roll; customer=$customer")
                }
                imageProxy.close()
            }
            .addOnFailureListener {
                runOnUiThread {
                    Toast.makeText(this, "OCR failed", Toast.LENGTH_SHORT).show()
                }
                imageProxy.close()
            }
    }

    // New, simple pattern-based extraction logic
    private fun extractFromText(text: Text): Pair<String, String> {
        val lines = text.textBlocks.flatMap { it.lines }
        Log.d("UETApp", "Full OCR text:\n${text.text}")

        // Print bounding boxes for debug
        lines.forEachIndexed { idx, line ->
            val box = line.boundingBox
            Log.d(
                "UETApp",
                "Line $idx: '${line.text}' box=(${box?.left},${box?.top},${box?.right},${box?.bottom}) w=${box?.width()} h=${box?.height()}"
            )
        }

        val rollRegex = Regex(""".*\d.*\d.*\d.*""") // at least 3 digits (used for most logic)
        val roll4Regex = Regex(""".*\d.*\d.*\d.*\d.*""") // at least 4 digits (for failsafe)
        val indicators = listOf(
            "LD", "SD", "F", "F1", "HD", "IE", "IE1", "SX", "IE2", "LA", "LA1", "LA2", "F2",
            "NO", "SB", "SD1", "SD2", "SD3", "SD4", "SDM", "SG1", "SGV", "SO", "SO1"
        )
        // Detects a line with either a bracket, indicator code, or both
        val indicatorRegex = Regex(
            """(\[.*?]|\(.*?\)|\{.*?\}|""" +
                    indicators.joinToString("|") { Regex.escape(it) } +
                    """)""",
            RegexOption.IGNORE_CASE
        )

        // 1. Find the indicator line (with bracket, indicator, or both)
        val indicatorIdx = lines.indexOfFirst { line ->
            indicatorRegex.containsMatchIn(line.text)
        }
        val cleanupRegex = Regex(
            """(\[.*?]|\(.*?\)|\{.*?\}|""" +
                    indicators.joinToString("|") { Regex.escape(it) } +
                    """)""",
            RegexOption.IGNORE_CASE
        )

        var roll = ""
        var rollIdx = -1
        var customer = ""

        if (indicatorIdx != -1) {
            // --- Main extraction logic as before ---
            val indicatorLine = lines[indicatorIdx].text.trim()
            if (rollRegex.containsMatchIn(indicatorLine)) {
                roll = indicatorLine.replace(cleanupRegex, "").replace("_", " ").trim()
                val digitGroup = Regex("""\d{3,}""").find(roll)?.value
                roll = digitGroup ?: roll
                rollIdx = indicatorIdx
            } else {
                // Search both above and below for roll
                val searchOrder = (indicatorIdx - 1 downTo 0) + (indicatorIdx + 1 until lines.size)
                for (i in searchOrder) {
                    val candidate = lines[i].text.trim()
                    if (rollRegex.containsMatchIn(candidate)) {
                        roll = candidate.replace(cleanupRegex, "").replace("_", " ").trim()
                        val digitGroup = Regex("""\d{3,}""").find(roll)?.value
                        roll = digitGroup ?: roll
                        rollIdx = i
                        break
                    }
                }
            }

            // Customer: next non-empty line (skip indicator and roll lines)
            val usedIndices = setOf(indicatorIdx, rollIdx)
            for (i in (rollIdx + 1) until lines.size) {
                if (i !in usedIndices) {
                    val t = lines[i].text.trim()
                    if (t.isNotEmpty()) {
                        customer = t.replace(cleanupRegex, "").replace("_", " ").trim()
                        break
                    }
                }
            }
            if (customer.isEmpty()) {
                for (i in (rollIdx - 1) downTo 0) {
                    if (i !in usedIndices) {
                        val t = lines[i].text.trim()
                        if (t.isNotEmpty()) {
                            customer = t.replace(cleanupRegex, "").replace("_", " ").trim()
                            break
                        }
                    }
                }
            }
        } else {
            // --- Failsafe extraction logic ---
            // Find the first line with 4 or more digits
            rollIdx = lines.indexOfFirst { line ->
                roll4Regex.containsMatchIn(line.text.trim())
            }
            if (rollIdx != -1) {
                val rollLine = lines[rollIdx].text.trim()
                val digitGroup = Regex("""\d{4,}""").find(rollLine)?.value
                roll = digitGroup ?: rollLine

                // Find customer: first line above or below (whichever comes first) with at least 3 chars
                var foundCustomer = false
                // Look below
                for (i in (rollIdx + 1) until lines.size) {
                    val t = lines[i].text.trim()
                    if (t.length >= 3) {
                        customer = t
                        foundCustomer = true
                        break
                    }
                }
                // If not found, look above
                if (!foundCustomer) {
                    for (i in (rollIdx - 1) downTo 0) {
                        val t = lines[i].text.trim()
                        if (t.length >= 3) {
                            customer = t
                            break
                        }
                    }
                }
            }
        }

        Log.d("UETApp", "Parsed roll: '$roll' customer: '$customer'")
        return roll to customer
    }


    private fun showResultPopup(roll: String, customer: String) {
        val view = layoutInflater.inflate(R.layout.dialog_result, null)
        view.findViewById<TextView>(R.id.tvRoll).text = roll
        view.findViewById<TextView>(R.id.tvCustomer).text = customer
        AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(true)
            .show()
    }

    private fun saveOcrLog(text: String) {
        try {
            val dir = File(filesDir, "ocr_logs").apply { if (!exists()) mkdirs() }
            File(dir, "ocr_log.txt").appendText(text + "\n---\n")
        } catch (e: IOException) {
            Log.e("UETApp", "Log write failed", e)
        }
    }

    private fun allPermissionsGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted()) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}

object ImageUtils {
    @androidx.camera.core.ExperimentalGetImage
    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val image = imageProxy.image
            ?: throw IllegalArgumentException("Image is null")
        return when (image.format) {
            ImageFormat.YUV_420_888 -> {
                val y = image.planes[0].buffer
                val u = image.planes[1].buffer
                val v = image.planes[2].buffer
                val ySize = y.remaining()
                val uSize = u.remaining()
                val vSize = v.remaining()
                val nv21 = ByteArray(ySize + uSize + vSize).apply {
                    y.get(this, 0, ySize)
                    v.get(this, ySize, vSize)
                    u.get(this, ySize + vSize, uSize)
                }
                YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null).let { yi ->
                    ByteArrayOutputStream().use { out ->
                        yi.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
                        BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
                    }
                }
            }
            ImageFormat.JPEG -> {
                val buf = image.planes[0].buffer
                val bytes = ByteArray(buf.remaining()).apply { buf.get(this) }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
            else -> throw IllegalArgumentException("Unsupported format: ${image.format}")
        }
    }
}
