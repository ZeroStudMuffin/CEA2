package com.example.app

import android.graphics.Bitmap
import android.util.Log
import com.example.app.TuningParams
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

    /**
     * Finds the label rectangle closest to [TuningParams.targetRatio] and
     * returns a perspective-warped bitmap. [fullImageArea] must be the
     * width × height of the captured photo so MIN_AREA is based on the
     * full frame instead of the cropped region. If detection fails the
     * original bitmap is returned.
     */
    fun cropLabel(bitmap: Bitmap, fullImageArea: Int): Bitmap {
        if (!OpenCVLoader.initDebug()) return bitmap

        val src = Mat()
        Utils.bitmapToMat(bitmap, src)

        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGBA2GRAY)
        if (TuningParams.useBlur) {
            Imgproc.GaussianBlur(
                gray,
                gray,
                Size(
                    TuningParams.blurKernel.toDouble(),
                    TuningParams.blurKernel.toDouble()
                ),
                0.0
            )
        }
        val edges = Mat()
        Imgproc.Canny(gray, edges, TuningParams.cannyLow.toDouble(), TuningParams.cannyHigh.toDouble())
        if (TuningParams.useDilate) {
            Imgproc.dilate(
                edges,
                edges,
                Imgproc.getStructuringElement(
                    Imgproc.MORPH_RECT,
                    Size(
                        TuningParams.dilateKernel.toDouble(),
                        TuningParams.dilateKernel.toDouble()
                    )
                )
            )
        }

        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(edges, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        val aspect = TuningParams.targetRatio
        Log.d(TAG, "Contours found: ${'$'}{contours.size}")

        var bestQuad: MatOfPoint2f? = null
        for (c in contours) {
            val poly = MatOfPoint2f()
            Imgproc.approxPolyDP(
                MatOfPoint2f(*c.toArray()),
                poly,
                if (TuningParams.useEpsilon) TuningParams.epsilon else 0.0,
                true
            )
            if (poly.total() == 4L && Imgproc.isContourConvex(MatOfPoint(*poly.toArray()))) {
                val r = Imgproc.boundingRect(c)
                val ratio = r.width.toFloat() / r.height
                val minArea = fullImageArea * TuningParams.minAreaRatio
                val areaOk = if (TuningParams.useMinArea) r.area() > minArea else true
                val ratioOk = if (TuningParams.useRatio) {
                    abs(ratio - aspect) < TuningParams.ratioTolerance * aspect
                } else true
                if (areaOk && ratioOk) {
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
        val w = TuningParams.outputWidth.toDouble()
        val h = TuningParams.outputHeight.toDouble()
        val dstPts = arrayOf(
            Point(0.0, 0.0),
            Point(w, 0.0),
            Point(w, h),
            Point(0.0, h)
        )
        val transform = Imgproc.getPerspectiveTransform(MatOfPoint2f(*srcPts), MatOfPoint2f(*dstPts))
        val label = Mat()
        Imgproc.warpPerspective(src, label, transform, Size(w, h))
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
