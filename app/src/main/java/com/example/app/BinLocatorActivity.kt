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
import android.util.Log
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BinLocatorActivity : AppCompatActivity() {
    private val TAG = "BinLocator"
    private lateinit var previewView: PreviewView
    private lateinit var overlay: BoundingBoxOverlay
    private lateinit var ocrTextView: TextView
    private lateinit var captureButton: Button
    private lateinit var rotateButton: ImageButton
    private lateinit var zoomSlider: Slider
    private lateinit var zoomResetButton: Button
    private lateinit var actionButtons: LinearLayout
    private lateinit var getReleaseButton: Button
    private lateinit var setBinButton: Button
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var controller: LifecycleCameraController
    private var cameraProvider: ProcessCameraProvider? = null
    private var lastBitmap: Bitmap? = null

    private val CAMERA_PERMISSION = Manifest.permission.CAMERA
    private val REQUEST_CAMERA_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bin_locator)
        previewView = findViewById(R.id.viewFinder)
        overlay = findViewById(R.id.boundingBox)
        ocrTextView = findViewById(R.id.ocrTextView)
        captureButton = findViewById(R.id.captureButton)
        rotateButton = findViewById(R.id.rotateButton)
        zoomSlider = findViewById(R.id.zoomSlider)
        zoomResetButton = findViewById(R.id.zoomResetButton)
        actionButtons = findViewById(R.id.actionButtons)
        getReleaseButton = findViewById(R.id.getReleaseButton)
        setBinButton = findViewById(R.id.setBinButton)
        cameraExecutor = Executors.newSingleThreadExecutor()

        rotateButton.setOnClickListener { toggleOrientation() }
        captureButton.setOnClickListener { takePhoto() }
        getReleaseButton.setOnClickListener { scanRelease() }
        setBinButton.setOnClickListener { showBinMenu() }

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
                val crop = overlay.mapToBitmapRect(rotated.width, rotated.height)
                Log.d(TAG, "Crop rect: $crop bitmap=${rotated.width}x${rotated.height}")
                val cropped = Bitmap.createBitmap(
                    rotated,
                    crop.left,
                    crop.top,
                    crop.width(),
                    crop.height()
                )
                lastBitmap = cropped
                val inputImage = InputImage.fromBitmap(cropped, 0)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(inputImage)
                    .addOnSuccessListener { result ->
                        for (block in result.textBlocks) {
                            for (line in block.lines) {
                                val height = line.boundingBox?.height() ?: -1
                                Log.d(TAG, "OCR line: '${line.text}' height=$height")
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
            Snackbar.make(previewView, "Bin: $bin", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showError(e: Exception) {
        e.printStackTrace()
        Snackbar.make(previewView, e.message ?: "Error", Snackbar.LENGTH_SHORT).show()
    }

    private fun toggleOrientation() {
        requestedOrientation = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
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
