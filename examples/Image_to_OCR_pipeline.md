// 1. (Optional) Undistort if using wide-angle/fisheye lens:
//    ——— You’d do this once at startup after calibrating your camera.
// val undistorted = Mat()
// Imgproc.undistort(inputMat, undistorted, cameraMatrix, distCoeffs)

val working = if (needUndistort) undistorted else inputMat

// 2. Grayscale → Blur → Canny
val gray = Mat()
Imgproc.cvtColor(working, gray, Imgproc.COLOR_RGBA2GRAY)
Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)
val edges = Mat()
Imgproc.Canny(gray, edges, 50.0, 150.0)

// 3. Dilate to close small gaps
Imgproc.dilate(edges, edges, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0)))

// 4. Find contours & approximate to polygons
val contours = mutableListOf<MatOfPoint>()
Imgproc.findContours(edges, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

var bestQuad: MatOfPoint2f? = null
for (c in contours) {
    val poly = MatOfPoint2f()
    Imgproc.approxPolyDP(MatOfPoint2f(c.toArray()), poly, 10.0, true)
    // keep only convex quads
    if (poly.total() == 4L && Imgproc.isContourConvex(MatOfPoint(*poly.toArray()))) {
        val r = Imgproc.boundingRect(c)
        val ratio = r.width.toDouble() / r.height
        if (abs(ratio - TARGET_RATIO) < RATIO_TOLERANCE && r.area() > MIN_AREA) {
            bestQuad = poly
            break
        }
    }
}

// 5. Perspective-warp to fixed W×H
if (bestQuad != null) {
    // sortCorners puts them in TL, TR, BR, BL order
    val srcPts = sortCorners(bestQuad.toArray())
    val dstPts = arrayOf(
        Point(0.0, 0.0),
        Point(LABEL_W.toDouble(), 0.0),
        Point(LABEL_W.toDouble(), LABEL_H.toDouble()),
        Point(0.0, LABEL_H.toDouble())
    )
    val M = Imgproc.getPerspectiveTransform(MatOfPoint2f(*srcPts), MatOfPoint2f(*dstPts))
    val labelMat = Mat()
    Imgproc.warpPerspective(working, labelMat, M, Size(LABEL_W.toDouble(), LABEL_H.toDouble()))

    // 6. Convert warped Mat → Bitmap → ML Kit InputImage
    val bmp = Bitmap.createBitmap(labelMat.cols(), labelMat.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(labelMat, bmp)
    val image = InputImage.fromBitmap(bmp, 0)

    // 7. Run ML Kit’s TextRecognizer
    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
      .process(image)
      .addOnSuccessListener { visionText ->
        // flatten all lines
        val lines = visionText.textBlocks.flatMap { it.lines }
        // parse out roll & customer
        val results = OcrParser.parse(lines)
        // results = ["Roll#:12345", "Cust:ACME_CORP"]
      }
      .addOnFailureListener { e ->
        Log.e("OCR", "failed", e)
      }
}
