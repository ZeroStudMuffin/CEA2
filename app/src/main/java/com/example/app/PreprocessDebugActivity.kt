package com.example.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/** Activity for inspecting OCR preprocessing result. */
class PreprocessDebugActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var overlay: BoundingBoxOverlay
    private lateinit var processedImage: ImageView
    private lateinit var captureButton: Button
    private lateinit var tuneButton: Button
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var controller: LifecycleCameraController
    private var cameraProvider: ProcessCameraProvider? = null

    private val CAMERA_PERMISSION = Manifest.permission.CAMERA
    private val REQUEST_CAMERA_PERMISSION = 3001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_preprocess_debug)
        previewView = findViewById(R.id.viewFinder)
        overlay = findViewById(R.id.boundingBox)
        processedImage = findViewById(R.id.processedImage)
        captureButton = findViewById(R.id.captureButton)
        tuneButton = findViewById(R.id.tuneButton)
        cameraExecutor = Executors.newSingleThreadExecutor()

        captureButton.setOnClickListener { takePhoto() }
        tuneButton.setOnClickListener { showTuningDialog() }

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
                bindToLifecycle(this@PreprocessDebugActivity)
            }
            previewView.controller = controller
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val photoFile = File.createTempFile("temp", ".jpg", cacheDir)
        val options = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        controller.takePicture(options, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val bmp = ImageUtils.decodeRotatedBitmap(photoFile)
                val crop = overlay.mapToBitmapRect(bmp.width, bmp.height)
                val warped = LabelCropper.cropLabel(
                    Bitmap.createBitmap(bmp, crop.left, crop.top, crop.width(), crop.height()),
                    bmp.width * bmp.height
                )
                val processed = ImageUtils.toGrayscale(warped)
                runOnUiThread { processedImage.setImageBitmap(processed) }
            }
        })
    }

    private fun showTuningDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_tuning, null)
        val blur = view.findViewById<com.google.android.material.slider.Slider>(R.id.blurSlider)
        val cannyLow = view.findViewById<com.google.android.material.slider.Slider>(R.id.cannyLowSlider)
        val cannyHigh = view.findViewById<com.google.android.material.slider.Slider>(R.id.cannyHighSlider)
        val dilate = view.findViewById<com.google.android.material.slider.Slider>(R.id.dilateSlider)
        val eps = view.findViewById<com.google.android.material.slider.Slider>(R.id.epsilonSlider)
        val minArea = view.findViewById<com.google.android.material.slider.Slider>(R.id.minAreaSlider)
        val ratioTol = view.findViewById<com.google.android.material.slider.Slider>(R.id.ratioSlider)
        val targetRatioEdit = view.findViewById<android.widget.EditText>(R.id.targetRatioEdit)
        val widthEdit = view.findViewById<android.widget.EditText>(R.id.widthEdit)
        val heightEdit = view.findViewById<android.widget.EditText>(R.id.heightEdit)
        val lineHeight = view.findViewById<com.google.android.material.slider.Slider>(R.id.lineHeightSlider)
        val blurCheck = view.findViewById<android.widget.CheckBox>(R.id.blurCheck)
        val dilateCheck = view.findViewById<android.widget.CheckBox>(R.id.dilateCheck)
        val epsilonCheck = view.findViewById<android.widget.CheckBox>(R.id.epsilonCheck)
        val minAreaCheck = view.findViewById<android.widget.CheckBox>(R.id.minAreaCheck)
        val ratioCheck = view.findViewById<android.widget.CheckBox>(R.id.ratioCheck)
        val targetRatioCheck = view.findViewById<android.widget.CheckBox>(R.id.targetRatioCheck)
        val lineHeightCheck = view.findViewById<android.widget.CheckBox>(R.id.lineHeightCheck)

        blur.value = TuningParams.blurKernel.toFloat()
        cannyLow.value = TuningParams.cannyLow.toFloat()
        cannyHigh.value = TuningParams.cannyHigh.toFloat()
        dilate.value = TuningParams.dilateKernel.toFloat()
        eps.value = TuningParams.epsilon.toFloat()
        minArea.value = TuningParams.minAreaRatio
        ratioTol.value = TuningParams.ratioTolerance
        targetRatioEdit.setText(TuningParams.targetRatio.toString())
        widthEdit.setText(TuningParams.outputWidth.toString())
        heightEdit.setText(TuningParams.outputHeight.toString())
        lineHeight.value = TuningParams.lineHeightPercent
        blurCheck.isChecked = TuningParams.useBlur
        blur.isEnabled = blurCheck.isChecked
        dilateCheck.isChecked = TuningParams.useDilate
        dilate.isEnabled = dilateCheck.isChecked
        epsilonCheck.isChecked = TuningParams.useEpsilon
        eps.isEnabled = epsilonCheck.isChecked
        minAreaCheck.isChecked = TuningParams.useMinArea
        minArea.isEnabled = minAreaCheck.isChecked
        ratioCheck.isChecked = TuningParams.useRatio
        ratioTol.isEnabled = ratioCheck.isChecked
        targetRatioCheck.isChecked = TuningParams.useRatio
        targetRatioEdit.isEnabled = ratioCheck.isChecked
        lineHeightCheck.isChecked = TuningParams.useLineHeight
        lineHeight.isEnabled = lineHeightCheck.isChecked

        minArea.setLabelFormatter { "${(it * 100).toInt()}%" }
        ratioTol.setLabelFormatter { "${(it * 100).toInt()}%" }

        val defaultTint = targetRatioCheck.buttonTintList
        ratioCheck.setOnCheckedChangeListener { _, checked ->
            ratioTol.isEnabled = checked
            targetRatioEdit.isEnabled = checked
            targetRatioCheck.isChecked = checked
            targetRatioCheck.buttonTintList =
                if (checked) defaultTint else android.content.res.ColorStateList.valueOf(android.graphics.Color.RED)
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Tune Pipeline")
            .setView(view)
            .create()

        view.findViewById<Button>(R.id.applyButton).setOnClickListener {
            TuningParams.useBlur = blurCheck.isChecked
            if (blurCheck.isChecked) {
                TuningParams.blurKernel = blur.value.toInt()
            }
            TuningParams.cannyLow = cannyLow.value.toInt()
            TuningParams.cannyHigh = cannyHigh.value.toInt()
            TuningParams.useDilate = dilateCheck.isChecked
            if (dilateCheck.isChecked) {
                TuningParams.dilateKernel = dilate.value.toInt()
            }
            TuningParams.useEpsilon = epsilonCheck.isChecked
            if (epsilonCheck.isChecked) {
                TuningParams.epsilon = eps.value.toDouble()
            }
            TuningParams.useMinArea = minAreaCheck.isChecked
            if (minAreaCheck.isChecked) {
                TuningParams.minAreaRatio = minArea.value
            }
            TuningParams.useRatio = ratioCheck.isChecked
            if (ratioCheck.isChecked) {
                TuningParams.ratioTolerance = ratioTol.value
                if (targetRatioCheck.isChecked) {
                    TuningParams.targetRatio = targetRatioEdit.text.toString().toFloatOrNull()
                        ?: TuningParams.targetRatio
                }
            }
            TuningParams.outputWidth = widthEdit.text.toString().toIntOrNull() ?: TuningParams.outputWidth
            TuningParams.outputHeight = heightEdit.text.toString().toIntOrNull() ?: TuningParams.outputHeight
            TuningParams.useLineHeight = lineHeightCheck.isChecked
            if (lineHeightCheck.isChecked) {
                TuningParams.lineHeightPercent = lineHeight.value
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
    }
}
