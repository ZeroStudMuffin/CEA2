package com.example.app

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.abs

/**
 * Utility for detecting and warping label images using OpenCV.
 */
object LabelCropper {
    private const val TAG = "LabelCropper"
    private const val LABEL_W = 800
    private const val LABEL_H = 200
    private const val RATIO_TOLERANCE = 0.15f

    /**
     * Finds the label rectangle closest to the given aspect ratio and
     * returns a perspective-warped bitmap. If detection fails the
     * original bitmap is returned.
     */
    fun cropLabel(bitmap: Bitmap, aspect: Float): Bitmap {
        if (!OpenCVLoader.initDebug()) return bitmap

        val src = Mat()
        Utils.bitmapToMat(bitmap, src)

        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGBA2GRAY)
        Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)
        val edges = Mat()
        Imgproc.Canny(gray, edges, 50.0, 150.0)
        Imgproc.dilate(edges, edges, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0)))

        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(edges, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        Log.d(TAG, "Contours found: ${'$'}{contours.size}")

        var bestQuad: MatOfPoint2f? = null
        for (c in contours) {
            val poly = MatOfPoint2f()
            Imgproc.approxPolyDP(MatOfPoint2f(*c.toArray()), poly, 10.0, true)
            if (poly.total() == 4L && Imgproc.isContourConvex(MatOfPoint(*poly.toArray()))) {
                val r = Imgproc.boundingRect(c)
                val ratio = r.width.toFloat() / r.height
                if (abs(ratio - aspect) < RATIO_TOLERANCE * aspect) {
                    bestQuad = poly
                    break
                }
            }
        }
        if (bestQuad == null) {
            Log.d(TAG, "No quad found")
            return bitmap
        }

        val srcPts = sortCorners(bestQuad.toArray())
        Log.d(TAG, "Warping label")
        val dstPts = arrayOf(
            Point(0.0, 0.0),
            Point(LABEL_W.toDouble(), 0.0),
            Point(LABEL_W.toDouble(), LABEL_H.toDouble()),
            Point(0.0, LABEL_H.toDouble())
        )
        val transform = Imgproc.getPerspectiveTransform(MatOfPoint2f(*srcPts), MatOfPoint2f(*dstPts))
        val label = Mat()
        Imgproc.warpPerspective(src, label, transform, Size(LABEL_W.toDouble(), LABEL_H.toDouble()))
        val result = Bitmap.createBitmap(label.cols(), label.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(label, result)
        return result
    }

    private fun sortCorners(pts: Array<Point>): Array<Point> {
        val sorted = pts.sortedBy { it.y }
        val top = sorted.take(2).sortedBy { it.x }
        val bottom = sorted.takeLast(2).sortedBy { it.x }.reversed()
        return arrayOf(top[0], top[1], bottom[0], bottom[1])
    }
}
