package com.example.app

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.view.View
import com.google.android.material.slider.Slider
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import com.example.app.BoundingBoxOverlay
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.example.app.ImageUtils
import com.example.app.ZoomUtils
import com.example.app.OcrParser
import com.example.app.RecordUploader
import com.example.app.BarcodeUtils
import com.example.app.LabelCropper
import com.example.app.DebugLogger
import com.example.app.BatchRecord
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import android.graphics.BitmapFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BinLocatorActivity : AppCompatActivity() {
    private val TAG = "BinLocator"
    private lateinit var previewView: PreviewView
    private lateinit var overlay: BoundingBoxOverlay
    private lateinit var ocrTextView: TextView
    private lateinit var captureButton: Button
    private lateinit var zoomSlider: Slider
    private lateinit var zoomResetButton: Button
    private lateinit var actionButtons: LinearLayout
    private lateinit var getReleaseButton: Button
    private lateinit var setBinButton: Button
    private lateinit var sendRecordButton: Button
    private lateinit var showOcrButton: Button
    private lateinit var showCropButton: Button
    private lateinit var showLogButton: Button
    private lateinit var addItemButton: Button
    private lateinit var showBatchButton: Button
    private lateinit var cropPreview: android.widget.ImageView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var controller: LifecycleCameraController
    private var cameraProvider: ProcessCameraProvider? = null
    private var lastBitmap: Bitmap? = null
    private var debugMode: Boolean = false
    private var batchMode: Boolean = false
    private val batchItems = mutableListOf<BatchRecord>()
    private var rawLines: List<String> = emptyList()

    private val CAMERA_PERMISSION = Manifest.permission.CAMERA
    private val REQUEST_CAMERA_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bin_locator)
        previewView = findViewById(R.id.viewFinder)
        overlay = findViewById(R.id.boundingBox)
        ocrTextView = findViewById(R.id.ocrTextView)
        captureButton = findViewById(R.id.captureButton)
        addItemButton = findViewById(R.id.addItemButton)
        zoomSlider = findViewById(R.id.zoomSlider)
        zoomResetButton = findViewById(R.id.zoomResetButton)
        actionButtons = findViewById(R.id.actionButtons)
        getReleaseButton = findViewById(R.id.getReleaseButton)
        setBinButton = findViewById(R.id.setBinButton)
        sendRecordButton = findViewById(R.id.sendRecordButton)
        showOcrButton = findViewById(R.id.showOcrButton)
        showCropButton = findViewById(R.id.showCropButton)
        showLogButton = findViewById(R.id.showLogButton)
        showBatchButton = findViewById(R.id.showBatchButton)
        cropPreview = findViewById(R.id.cropPreview)
        cameraExecutor = Executors.newSingleThreadExecutor()

        debugMode = intent.getBooleanExtra("debug", false)
        batchMode = intent.getBooleanExtra("batch", true)
        if (batchMode) {
            addItemButton.visibility = View.VISIBLE
            showBatchButton.visibility = View.VISIBLE
        }
        if (debugMode) {
            showOcrButton.visibility = View.VISIBLE
            showCropButton.visibility = View.VISIBLE
            showLogButton.visibility = View.VISIBLE
        }
        sendRecordButton.isEnabled = false
        sendRecordButton.alpha = 0.5f

        captureButton.setOnClickListener { takePhoto() }
        getReleaseButton.setOnClickListener { scanRelease() }
        setBinButton.setOnClickListener { showBinMenu() }
        sendRecordButton.setOnClickListener { sendRecord() }
        addItemButton.setOnClickListener { onAddItem() }
        showBatchButton.setOnClickListener { showBatchItems() }
        showOcrButton.setOnClickListener { showRawOcr() }
        showCropButton.setOnClickListener { toggleCropPreview() }
        showLogButton.setOnClickListener { showDebugLog() }

        if (ActivityCompat.checkSelfPermission(this, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA_PERMISSION), REQUEST_CAMERA_PERMISSION)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            controller = LifecycleCameraController(this).apply {
                setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
                bindToLifecycle(this@BinLocatorActivity)
            }
            previewView.controller = controller

            zoomSlider.addOnChangeListener { _, value, _ ->
                val clamped = ZoomUtils.clampLinearZoom(value)
                controller.setLinearZoom(clamped)
            }

            zoomResetButton.setOnClickListener {
                controller.setZoomRatio(1f)
            }

            controller.zoomState.observe(this) { state ->
                val clamped = ZoomUtils.clampZoomRatio(state.zoomRatio)
                if (clamped != state.zoomRatio) {
                    controller.setZoomRatio(clamped)
                }
                if (zoomSlider.value != state.linearZoom) {
                    zoomSlider.value = state.linearZoom
                }
            }
        }, ContextCompat.getMainExecutor(this))
    }


    private fun takePhoto() {
        val photoFile = File.createTempFile("temp", ".jpg", cacheDir)
        val options = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        controller.takePicture(options, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                showError(exception)
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val rotated = ImageUtils.decodeRotatedBitmap(photoFile)
                val viewRect = overlay.getCropRect()
                val box = BoundingBoxOverlay.scaleRect(
                    viewRect,
                    overlay.width,
                    overlay.height,
                    rotated.width,
                    rotated.height
                )
                DebugLogger.log("Overlay rect=$viewRect mapped=$box")
                val cropped = Bitmap.createBitmap(
                    rotated,
                    box.left,
                    box.top,
                    box.width(),
                    box.height()
                )
                val refined = LabelCropper.refineCrop(cropped)
                lastBitmap = refined
                if (debugMode) {
                    DebugLogger.log("Refined crop ${refined.width}x${refined.height}")
                    val outFile = File(cacheDir, "ocr_debug.png")
                    FileOutputStream(outFile).use { stream ->
                        // Reason: PNG is lossless so the saved debug image is
                        // pixel-identical to the bitmap passed to ML Kit.
                        refined.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                    DebugLogger.log("Saved debug image to ${outFile.absolutePath}")
                }
                val inputImage = InputImage.fromBitmap(refined, 0)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(inputImage)
                    .addOnSuccessListener { result ->
                        rawLines = result.textBlocks.flatMap { block ->
                            block.lines.map { line ->
                                val height = line.boundingBox?.height() ?: -1
                                Log.d(TAG, "OCR line: '${line.text}' height=$height")
                                "${line.text} (h=$height)"
                            }
                        }

                        val parsed = OcrParser.parse(result.textBlocks.flatMap { it.lines })
                        for (clean in parsed) {
                            Log.d(TAG, "Parsed line: '$clean'")
                        }
                        showResult(parsed)
                    }
                    .addOnFailureListener { showError(it) }
            }
        })
    }

    private fun showResult(lines: List<String>) {
        runOnUiThread {
            ocrTextView.text = lines.joinToString("\n")
            actionButtons.visibility = View.VISIBLE
            updateSendRecordVisibility()
        }
    }

    private fun scanRelease() {
        val bitmap = lastBitmap
        if (bitmap == null) {
            Log.d(TAG, "scanRelease called with no bitmap")
            return
        }
        Log.d(TAG, "Starting release scan with bitmap ${bitmap.width}x${bitmap.height}")
        val image = InputImage.fromBitmap(bitmap, 0)
        val client = BarcodeScanning.getClient()
        client.process(image)
            .addOnSuccessListener { barcodes ->
                Log.d(TAG, "Barcode scan success: ${barcodes.size} codes")
                for ((index, code) in barcodes.withIndex()) {
                    Log.d(
                        TAG,
                        "code[$index] format=${code.format} value=${code.rawValue} bounds=${code.boundingBox}"
                    )
                }
                val release = BarcodeUtils.extractRelease(barcodes)
                if (release != null) {
                    Snackbar.make(previewView, "Release: $release", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(previewView, "no release found", Snackbar.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Barcode scan failed", it)
                showError(it)
            }
    }

    private fun showBinMenu() {
        val bins = (19..65).map(Int::toString) + listOf("Floor BR", "Floor BL")
        runOnUiThread {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Bin")
                .setItems(bins.toTypedArray()) { _, which ->
                    applyBin(bins[which])
                }
                .show()
        }
    }

    private fun applyBin(bin: String) {
        runOnUiThread {
            val lines = ocrTextView.text.split("\n").toMutableList()
            val rollIndex = lines.indexOfFirst { it.startsWith("Roll#:") }

            if (rollIndex >= 0) {
                val withoutBin = lines[rollIndex].replace(Regex("\\s+BIN=.*"), "")
                lines[rollIndex] = "$withoutBin BIN=$bin"
                lines.removeAll { it.trim().startsWith("BIN=") && lines.indexOf(it) != rollIndex }
            } else {
                val existing = lines.indexOfFirst { it.trim().startsWith("BIN=") }
                if (existing >= 0) {
                    lines[existing] = "BIN=$bin"
                } else {
                    lines.add("BIN=$bin")
                }
            }

            ocrTextView.text = lines.joinToString("\n")
            if (batchMode) {
                for (item in batchItems) {
                    item.bin = bin
                }
            }
            Snackbar.make(previewView, "Bin: $bin", Snackbar.LENGTH_SHORT).show()
            updateSendRecordVisibility()
        }
    }

    private fun updateSendRecordVisibility() {
        val textLines = ocrTextView.text.split("\n")
        val hasRoll = textLines.any { it.startsWith("Roll#:") }
        val hasCust = textLines.any { it.startsWith("Cust:") }
        val hasBin = textLines.any { it.contains("BIN=") }
        val batchReady = batchMode && batchItems.isNotEmpty() && batchItems.all { it.bin != null }
        val enabled = !debugMode && ((hasRoll && hasCust && hasBin) || batchReady)
        sendRecordButton.isEnabled = enabled
        sendRecordButton.alpha = if (enabled) 1f else 0.5f
    }

    private fun sendRecord() {
        if (debugMode) {
            Snackbar.make(previewView, "Debug mode - record not sent", Snackbar.LENGTH_SHORT).show()
            return
        }
        val payloads = mutableListOf<Triple<String, String, String>>()
        val lines = ocrTextView.text.split("\n")
        val rollLine = lines.firstOrNull { it.startsWith("Roll#:") }?.substringAfter("Roll#:")?.trim()
        val roll = rollLine?.replace(Regex("\\s*BIN=.*"), "")?.trim()
        val customer = lines.firstOrNull { it.startsWith("Cust:") }?.substringAfter("Cust:")?.trim()
        val bin = lines.firstOrNull { it.contains("BIN=") }?.substringAfter("BIN=")?.trim()
        if (roll != null && customer != null && bin != null) {
            payloads += Triple(roll, customer, bin)
        }
        if (batchMode) {
            batchItems.mapNotNullTo(payloads) { item ->
                val b = item.bin
                if (b == null) null else Triple(item.roll, item.customer, b)
            }
        }
        if (payloads.isEmpty()) return
        for ((r, c, b) in payloads) {
            RecordUploader.sendRecord(r, c, b) { success, message ->
                runOnUiThread {
                    val text = if (success) message ?: "Record sent" else message ?: "Send failed"
                    Snackbar.make(previewView, text, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        runOnUiThread {
            batchItems.clear()
            ocrTextView.text = ""
            actionButtons.visibility = View.GONE
            sendRecordButton.isEnabled = false
            sendRecordButton.alpha = 0.5f
        }
    }

    private fun showRawOcr() {
        if (rawLines.isEmpty()) return
        runOnUiThread {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Raw OCR")
                .setMessage(rawLines.joinToString("\n"))
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun showDebugLog() {
        val logs = DebugLogger.getLogs()
        if (logs.isEmpty()) return
        runOnUiThread {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Debug Log")
                .setMessage(logs.joinToString("\n"))
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun toggleCropPreview() {
        val bitmap = if (debugMode) {
            val file = File(cacheDir, "ocr_debug.png")
            if (file.exists()) android.graphics.BitmapFactory.decodeFile(file.absolutePath) else lastBitmap
        } else {
            lastBitmap
        } ?: return
        runOnUiThread {
            if (cropPreview.visibility == View.GONE) {
                cropPreview.setImageBitmap(bitmap)
                cropPreview.colorFilter = null
                cropPreview.visibility = View.VISIBLE
                overlay.visibility = View.GONE
            } else {
                cropPreview.visibility = View.GONE
                overlay.visibility = View.VISIBLE
            }
        }
    }

    private fun onAddItem() {
        val lines = ocrTextView.text.split("\n")
        val roll = lines.firstOrNull { it.startsWith("Roll#:") }?.substringAfter("Roll#:")?.replace(Regex("\\s*BIN=.*"), "")?.trim()
        val cust = lines.firstOrNull { it.startsWith("Cust:") }?.substringAfter("Cust:")?.trim()
        val bin = lines.firstOrNull { it.contains("BIN=") }?.substringAfter("BIN=")?.trim()
        if (roll != null && cust != null) {
            batchItems += BatchRecord(roll, cust, bin)
            ocrTextView.text = ""
            actionButtons.visibility = View.GONE
            updateSendRecordVisibility()
        }
    }

    private fun showBatchItems() {
        if (batchItems.isEmpty()) return
        val message = batchItems.joinToString("\n") { "${it.roll} - ${it.customer} - ${it.bin ?: "no bin"}" }
        runOnUiThread {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Queued Items")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun showError(e: Exception) {
        e.printStackTrace()
        Snackbar.make(previewView, e.message ?: "Error", Snackbar.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            showError(SecurityException("Camera permission denied"))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
    }
}
