package com.example.app

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
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
import com.example.app.LabelCropper
import com.example.app.ZoomUtils
import com.example.app.OcrParser
import com.example.app.RecordUploader
import com.example.app.CheckoutUtils
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
    private lateinit var zoomSlider: Slider
    private lateinit var actionButtons: LinearLayout
    private lateinit var getReleaseButton: Button
    private lateinit var setBinButton: Button
    private lateinit var sendRecordButton: Button
    private lateinit var showOcrButton: Button
    private lateinit var showCropButton: Button
    private lateinit var tuneButton: Button
    private lateinit var showLogButton: Button
    private lateinit var addItemButton: Button
    private lateinit var inputItemButton: Button
    private lateinit var showBatchButton: Button
    private lateinit var cropPreview: android.widget.ImageView
    private lateinit var binMenuContainer: android.widget.FrameLayout
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var controller: LifecycleCameraController
    private var cameraProvider: ProcessCameraProvider? = null
    private var lastBitmap: Bitmap? = null
    private var debugMode: Boolean = false
    private var batchMode: Boolean = false
    private lateinit var pin: String
    private val batchItems = mutableListOf<BatchRecord>()
    private var manualRoll: String? = null
    private var manualCustomer: String? = null
    private var rawLines: List<String> = emptyList()
    private val debugLog = StringBuilder()

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
        inputItemButton = findViewById(R.id.inputItemButton)
        zoomSlider = findViewById(R.id.zoomSlider)
        actionButtons = findViewById(R.id.actionButtons)
        getReleaseButton = findViewById(R.id.getReleaseButton)
        setBinButton = findViewById(R.id.setBinButton)
        sendRecordButton = findViewById(R.id.sendRecordButton)
        showOcrButton = findViewById(R.id.showOcrButton)
        showCropButton = findViewById(R.id.showCropButton)
        tuneButton = findViewById(R.id.tuneButton)
        showLogButton = findViewById(R.id.showLogButton)
        showBatchButton = findViewById(R.id.showBatchButton)
        cropPreview = findViewById(R.id.cropPreview)
        binMenuContainer = findViewById(R.id.binMenuContainer)
        cameraExecutor = Executors.newSingleThreadExecutor()

        debugMode = intent.getBooleanExtra("debug", false)
        batchMode = intent.getBooleanExtra("batch", false)
        pin = intent.getStringExtra("pin") ?: ""
        if (batchMode) {
            actionButtons.visibility = View.VISIBLE
            addItemButton.visibility = View.VISIBLE
            inputItemButton.visibility = View.VISIBLE
            showBatchButton.visibility = View.VISIBLE
            sendRecordButton.visibility = View.VISIBLE
        } else {
            actionButtons.visibility = View.GONE
            getReleaseButton.visibility = View.GONE
            setBinButton.visibility = View.GONE
            sendRecordButton.visibility = View.GONE
            inputItemButton.visibility = View.VISIBLE
        }
        if (debugMode) {
            showOcrButton.visibility = View.VISIBLE
            showCropButton.visibility = View.VISIBLE
            showLogButton.visibility = View.VISIBLE
            tuneButton.visibility = View.VISIBLE
        }
        sendRecordButton.isEnabled = false
        sendRecordButton.alpha = 0.5f

        captureButton.setOnClickListener { takePhoto() }
        getReleaseButton.setOnClickListener { scanRelease() }
        setBinButton.setOnClickListener { showBinMenu() }
        sendRecordButton.setOnClickListener { sendRecord() }
        addItemButton.setOnClickListener { onAddItem() }
        inputItemButton.setOnClickListener { showInputItemDialog() }
        showBatchButton.setOnClickListener { showBatchItems() }
        showOcrButton.setOnClickListener { showRawOcr() }
        showCropButton.setOnClickListener { toggleCropPreview() }
        showLogButton.setOnClickListener { showDebugLog() }
        tuneButton.setOnClickListener { showTuningDialog() }

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

            controller.setLinearZoom(0.4f)
            zoomSlider.value = 0.4f

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
                dLog("Initial crop ${crop.width()}x${crop.height()}")
                val fullArea = rotated.width * rotated.height
                val warped = LabelCropper.cropLabel(cropped, fullArea)
                dLog("Warped size ${warped.width}x${warped.height}")
                val processed = ImageUtils.toGrayscale(warped)
                lastBitmap = processed
                if (debugMode) {
                    saveDebugImage(processed)
                }
                val inputImage = InputImage.fromBitmap(processed, 0)
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
            actionButtons.visibility = if (batchMode) View.VISIBLE else View.GONE
            updateSendRecordVisibility()
            if (!batchMode) {
                val hasRoll = lines.any { it.startsWith("Roll#:") }
                val hasCust = lines.any { it.startsWith("Cust:") }
                if (hasRoll && hasCust) {
                    showBinOverlay()
                }
            }
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
        sendRecordButton.visibility = if (batchMode) View.VISIBLE else View.GONE
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
            RecordUploader.sendRecord(r, c, b, pin) { success, message ->
                runOnUiThread {
                    val text = if (success) message ?: "Record sent" else message ?: "Send failed"
                    Snackbar.make(previewView, text, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        runOnUiThread {
            batchItems.clear()
            ocrTextView.text = ""
            if (!batchMode) {
                actionButtons.visibility = View.GONE
            }
            clearManualQueue()
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

    private fun toggleCropPreview() {
        val bmp = if (debugMode) {
            val file = File(cacheDir, "warped.jpg")
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else lastBitmap
        } else {
            lastBitmap
        } ?: return
        runOnUiThread {
            if (cropPreview.visibility == View.GONE) {
                dLog("Showing crop preview")
                saveDebugImage(bmp)
                cropPreview.setImageBitmap(bmp)
                cropPreview.clearColorFilter()
                cropPreview.visibility = View.VISIBLE
                overlay.visibility = View.GONE
            } else {
                dLog("Hiding crop preview")
                cropPreview.clearColorFilter()
                cropPreview.visibility = View.GONE
                overlay.visibility = View.VISIBLE
            }
        }
    }

    /** Shows a dialog allowing runtime tuning of OCR parameters. */
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

    /** Displays the collected debug log messages. */
    private fun showDebugLog() {
        val text = debugLog.toString()
        if (text.isEmpty()) return
        runOnUiThread {
            androidx.appcompat.app.AlertDialog.Builder(this)
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

    /**
     * Saves the given bitmap to cache for debugging purposes.
     */
    private fun saveDebugImage(bmp: Bitmap) {
        try {
            File(cacheDir, "warped.jpg").outputStream().use { out ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            dLog("Debug image saved")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save debug image", e)
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
            if (!batchMode) {
                actionButtons.visibility = View.GONE
            }
            updateSendRecordVisibility()
            clearManualQueue()
        }
    }

    private fun showInputItemDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_input_item, null)
        val rollEdit = view.findViewById<android.widget.EditText>(R.id.rollEditText)
        val custEdit = view.findViewById<android.widget.EditText>(R.id.customerEditText)
        rollEdit.setText(manualRoll ?: "")
        custEdit.setText(manualCustomer ?: "")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Input Item")
            .setView(view)
            .setPositiveButton("Accept") { _, _ ->
                val r = rollEdit.text.toString().trim()
                val c = custEdit.text.toString().trim()
                if (r.isNotEmpty() && c.isNotEmpty()) {
                    batchItems += BatchRecord(r, c, null)
                    ocrTextView.text = ""
                    if (!batchMode) actionButtons.visibility = View.GONE
                    updateSendRecordVisibility()
                } else {
                    manualRoll = r
                    manualCustomer = c
                    return@setPositiveButton
                }
                clearManualQueue()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearManualQueue() {
        manualRoll = null
        manualCustomer = null
    }

    private fun showBatchItems() {
        val items = CheckoutUtils.buildPayload(ocrTextView.text.split("\n"), batchItems)
        if (items.isEmpty()) return
        val message = items.joinToString("\n") { "${it.roll} - ${it.customer} - ${it.bin ?: "no bin"}" }
        runOnUiThread {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Queued Items")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun showBinOverlay() {
        val view = layoutInflater.inflate(R.layout.dialog_bins, binMenuContainer, false)
        captureButton.visibility = View.GONE
        zoomSlider.visibility = View.GONE
        view.findViewById<View>(R.id.overlayBackground).setOnClickListener {
            binMenuContainer.removeAllViews()
            binMenuContainer.visibility = View.GONE
            captureButton.visibility = View.VISIBLE
            zoomSlider.visibility = View.VISIBLE
        }
        for (i in 9..65) {
            val resId = resources.getIdentifier("bin$i", "id", packageName)
            view.findViewById<Button>(resId)?.setOnClickListener {
                applyBin(i.toString())
                sendRecord()
                binMenuContainer.removeAllViews()
                binMenuContainer.visibility = View.GONE
                captureButton.visibility = View.VISIBLE
                zoomSlider.visibility = View.VISIBLE
            }
        }
        for (i in 1..4) {
            val resId = resources.getIdentifier("binF$i", "id", packageName)
            view.findViewById<Button>(resId)?.setOnClickListener {
                applyBin("F$i")
                sendRecord()
                binMenuContainer.removeAllViews()
                binMenuContainer.visibility = View.GONE
                captureButton.visibility = View.VISIBLE
                zoomSlider.visibility = View.VISIBLE
            }
        }
        binMenuContainer.addView(view)
        binMenuContainer.visibility = View.VISIBLE
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
