package com.example.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.core.Core
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Activity showing a live edge preview using CameraX ImageAnalysis.
 */
class LiveEdgePreviewActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var overlay: BoundingBoxOverlay
    private lateinit var edgeView: ImageView
    private lateinit var processedImage: ImageView
    private lateinit var captureButton: Button
    private lateinit var tuneButton: Button
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var controller: LifecycleCameraController
    private var cameraProvider: ProcessCameraProvider? = null

    private val CAMERA_PERMISSION = Manifest.permission.CAMERA
    private val REQUEST_CAMERA_PERMISSION = 4001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_live_edge_preview)
        previewView = findViewById(R.id.viewFinder)
        overlay = findViewById(R.id.boundingBox)
        edgeView = findViewById(R.id.edgeView)
        processedImage = findViewById(R.id.processedImage)
        captureButton = findViewById(R.id.captureButton)
        tuneButton = findViewById(R.id.tuneButton)
        cameraExecutor = Executors.newSingleThreadExecutor()
        tuneButton.setOnClickListener { showTuningDialog() }
        captureButton.setOnClickListener { takePhoto() }
        captureButton.isEnabled = false
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
                setEnabledUseCases(
                    LifecycleCameraController.IMAGE_ANALYSIS or
                        LifecycleCameraController.IMAGE_CAPTURE
                )
                cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
                setImageAnalysisAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { image ->
                    processFrame(image)
                })
                bindToLifecycle(this@LiveEdgePreviewActivity)
            }
            previewView.controller = controller
            controller.initializationFuture.addListener(
                { captureButton.isEnabled = true },
                ContextCompat.getMainExecutor(this)
            )
        }, ContextCompat.getMainExecutor(this))
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        val yuv = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuv.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val data = out.toByteArray()
        return android.graphics.BitmapFactory.decodeByteArray(data, 0, data.size)
    }

    private fun processFrame(image: ImageProxy) {
        try {
            if (!OpenCVLoader.initDebug()) return
            val bmp = imageProxyToBitmap(image)
            val mat = Mat()
            Utils.bitmapToMat(bmp, mat)
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY)
            if (TuningParams.useBlur) {
                val k = if (TuningParams.blurKernel % 2 == 1) TuningParams.blurKernel else TuningParams.blurKernel + 1
                Imgproc.GaussianBlur(mat, mat, Size(k.toDouble(), k.toDouble()), 0.0)
            }
            Imgproc.Canny(mat, mat, TuningParams.cannyLow.toDouble(), TuningParams.cannyHigh.toDouble())
            if (TuningParams.useDilate) {
                Imgproc.dilate(
                    mat,
                    mat,
                    Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(TuningParams.dilateKernel.toDouble(), TuningParams.dilateKernel.toDouble()))
                )
            }
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2RGBA)
            Core.rotate(mat, mat, Core.ROTATE_90_CLOCKWISE)
            val result = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(mat, result)
            runOnUiThread { edgeView.setImageBitmap(result) }
        } catch (e: Exception) {
            Log.e("LiveEdgePreview", "processFrame failed", e)
        } finally {
            image.close()
        }
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

    private fun takePhoto() {
        val photoFile = java.io.File.createTempFile("temp", ".jpg", cacheDir)
        val options = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        controller.takePicture(options, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                Log.e("LiveEdgePreview", "capture failed", exception)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val rotated = ImageUtils.decodeRotatedBitmap(photoFile)
                val crop = overlay.mapToBitmapRect(rotated.width, rotated.height)
                val cropped = android.graphics.Bitmap.createBitmap(
                    rotated,
                    crop.left,
                    crop.top,
                    crop.width(),
                    crop.height()
                )
                val warped = LabelCropper.cropLabel(cropped, rotated.width * rotated.height)
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
            targetRatioCheck.buttonTintList = if (checked) defaultTint else android.content.res.ColorStateList.valueOf(android.graphics.Color.RED)
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Tune Pipeline")
            .setView(view)
            .create()

        view.findViewById<Button>(R.id.applyButton).setOnClickListener {
            TuningParams.useBlur = blurCheck.isChecked
            if (blurCheck.isChecked) {
                var kernel = blur.value.toInt()
                if (kernel % 2 == 0) kernel++
                TuningParams.blurKernel = kernel
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
                    TuningParams.targetRatio = targetRatioEdit.text.toString().toFloatOrNull() ?: TuningParams.targetRatio
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
}
