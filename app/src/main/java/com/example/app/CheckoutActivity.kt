package com.example.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Activity for checking out queued items using the camera OCR pipeline.
 */
class CheckoutActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var overlay: BoundingBoxOverlay
    private lateinit var ocrTextView: TextView
    private lateinit var captureButton: Button
    private lateinit var addItemButton: Button
    private lateinit var showBatchButton: Button
    private lateinit var showLogButton: Button
    private lateinit var checkoutButton: Button
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var controller: LifecycleCameraController
    private var cameraProvider: ProcessCameraProvider? = null
    private var lastBitmap: Bitmap? = null
    private val batchItems = mutableListOf<BatchRecord>()
    private lateinit var pin: String
    private var debugMode: Boolean = false
    private val debugLog = StringBuilder()
    private val TAG = "CheckoutActivity"

    private val CAMERA_PERMISSION = Manifest.permission.CAMERA
    private val REQUEST_CAMERA_PERMISSION = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        previewView = findViewById(R.id.viewFinder)
        overlay = findViewById(R.id.boundingBox)
        ocrTextView = findViewById(R.id.ocrTextView)
        captureButton = findViewById(R.id.captureButton)
        addItemButton = findViewById(R.id.addItemButton)
        showBatchButton = findViewById(R.id.showBatchButton)
        showLogButton = findViewById(R.id.showLogButton)
        checkoutButton = findViewById(R.id.checkoutButton)
        cameraExecutor = Executors.newSingleThreadExecutor()
        pin = intent.getStringExtra("pin") ?: ""
        debugMode = intent.getBooleanExtra("debug", false)
        // Show the queue controls since checkout always runs in batch mode
        addItemButton.visibility = android.view.View.VISIBLE
        showBatchButton.visibility = android.view.View.VISIBLE
        if (debugMode) {
            showLogButton.visibility = View.VISIBLE
        }

        captureButton.setOnClickListener { takePhoto() }
        addItemButton.setOnClickListener { onAddItem() }
        showBatchButton.setOnClickListener { showBatchItems() }
        checkoutButton.setOnClickListener { confirmCheckout() }
        showLogButton.setOnClickListener { showDebugLog() }
        checkoutButton.isEnabled = false
        checkoutButton.alpha = 0.5f
        checkoutButton.visibility = android.view.View.GONE

        if (ActivityCompat.checkSelfPermission(this, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA_PERMISSION), REQUEST_CAMERA_PERMISSION)
        }
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            cameraProvider = future.get()
            controller = LifecycleCameraController(this).apply {
                setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
                bindToLifecycle(this@CheckoutActivity)
            }
            previewView.controller = controller
            controller.setLinearZoom(0.4f)
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
                val cropped = Bitmap.createBitmap(rotated, crop.left, crop.top, crop.width(), crop.height())
                dLog("Initial crop ${crop.width()}x${crop.height()}")
                val warped = LabelCropper.cropLabel(cropped, overlay.aspectRatio())
                dLog("Warped size ${warped.width}x${warped.height}")
                val processed = ImageUtils.toGrayscale(warped)
                if (debugMode) {
                    saveDebugImage(processed)
                }
                lastBitmap = processed
                val inputImage = InputImage.fromBitmap(processed, 0)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(inputImage)
                    .addOnSuccessListener { result ->
                        val parsed = OcrParser.parse(result.textBlocks.flatMap { it.lines })
                        for (line in parsed) {
                            dLog("Parsed line: '$line'")
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
            for (line in lines) {
                dLog("Result line: '$line'")
            }
            updateCheckoutButton()
        }
    }

    private fun onAddItem() {
        val lines = ocrTextView.text.split("\n")
        val roll = lines.firstOrNull { it.startsWith("Roll#:") }?.substringAfter(":")?.trim()
        val cust = lines.firstOrNull { it.startsWith("Cust:") }?.substringAfter(":")?.trim()
        if (roll != null && cust != null) {
            batchItems += BatchRecord(roll, cust, null)
            ocrTextView.text = ""
            updateCheckoutButton()
        }
    }

    private fun showBatchItems() {
        if (batchItems.isEmpty()) return
        val message = batchItems.joinToString("\n") { "${it.roll} - ${it.customer}" }
        AlertDialog.Builder(this)
            .setTitle("Queued Items")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun updateCheckoutButton() {
        val lines = ocrTextView.text.split("\n")
        val hasRoll = lines.any { it.startsWith("Roll#:") }
        val hasCust = lines.any { it.startsWith("Cust:") }
        val enabled = batchItems.isNotEmpty() || (hasRoll && hasCust)
        checkoutButton.isEnabled = enabled
        checkoutButton.alpha = if (enabled) 1f else 0.5f
        checkoutButton.visibility = if (enabled) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun confirmCheckout() {
        val count = batchItems.size
        AlertDialog.Builder(this)
            .setTitle("Checkout")
            .setMessage("Checkout $count item(s)?")
            .setPositiveButton("Yes") { _, _ -> sendCheckout() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun sendCheckout() {
        dLog("Sending ${batchItems.size} items")
        for (item in batchItems) {
            dLog("checkout roll=${item.roll}, customer=${item.customer}")
        }
        CheckoutUploader.checkoutItems(batchItems.toList(), pin) { success, message ->
            runOnUiThread {
                val text = message ?: if (success) "Checkout complete" else "Checkout failed"
                Snackbar.make(previewView, text, Snackbar.LENGTH_SHORT).show()
                if (success) {
                    batchItems.clear()
                    ocrTextView.text = ""
                    updateCheckoutButton()
                }
            }
        }
    }

    private fun showError(e: Exception) {
        e.printStackTrace()
        Snackbar.make(previewView, e.message ?: "Error", Snackbar.LENGTH_SHORT).show()
    }

    /** Displays the collected debug log messages. */
    private fun showDebugLog() {
        val text = debugLog.toString()
        if (text.isEmpty()) return
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Debug Log")
                .setMessage(text)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    /** Adds a message to the in-memory debug log and logcat. */
    private fun dLog(msg: String) {
        Log.d(TAG, msg)
        debugLog.append(msg).append('\n')
    }

    /** Saves the given bitmap to cache for debugging purposes. */
    private fun saveDebugImage(bmp: Bitmap) {
        try {
            File(cacheDir, "checkout_warped.jpg").outputStream().use { out ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            dLog("Debug image saved")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save debug image", e)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
