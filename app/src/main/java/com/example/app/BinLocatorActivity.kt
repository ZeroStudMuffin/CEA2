package com.example.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import com.google.android.material.slider.Slider
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import com.example.app.ImageUtils
import com.example.app.ZoomUtils
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BinLocatorActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var overlay: BoundingBoxOverlay
    private lateinit var captureButton: Button
    private lateinit var rotateButton: ImageButton
    private lateinit var zoomSlider: Slider
    private lateinit var zoomResetButton: Button
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var controller: LifecycleCameraController
    private var rotation: Int = 0
    private var cameraProvider: ProcessCameraProvider? = null

    private val CAMERA_PERMISSION = Manifest.permission.CAMERA
    private val REQUEST_CAMERA_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bin_locator)
        previewView = findViewById(R.id.viewFinder)
        overlay = findViewById(R.id.boundingBox)
        captureButton = findViewById(R.id.captureButton)
        rotateButton = findViewById(R.id.rotateButton)
        zoomSlider = findViewById(R.id.zoomSlider)
        zoomResetButton = findViewById(R.id.zoomResetButton)
        cameraExecutor = Executors.newSingleThreadExecutor()

        rotateButton.setOnClickListener { rotation = (rotation + 90) % 360 }
        captureButton.setOnClickListener { takePhoto() }

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
                controller.setLinearZoom(value)
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
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                val rotated = ImageUtils.rotateBitmap(bitmap, rotation)
                val crop = overlay.mapToBitmapRect(rotated.width, rotated.height)
                val cropped = Bitmap.createBitmap(
                    rotated,
                    crop.left,
                    crop.top,
                    crop.width(),
                    crop.height()
                )
                val inputImage = InputImage.fromBitmap(cropped, 0)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(inputImage)
                    .addOnSuccessListener { result -> showResult(result.text) }
                    .addOnFailureListener { showError(it) }
            }
        })
    }

    private fun showResult(text: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setMessage(text)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun showError(e: Exception) {
        e.printStackTrace()
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
