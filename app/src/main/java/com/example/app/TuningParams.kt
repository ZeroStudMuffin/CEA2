package com.example.app

/**
 * Runtime-adjustable parameters for the OCR pipeline.
 */
object TuningParams {
    var blurKernel: Int = 5
    var cannyLow: Int = 50
    var cannyHigh: Int = 150
    var dilateKernel: Int = 3
    var epsilon: Double = 10.0
    var minAreaRatio: Float = 0.1f
    /** Target width/height ratio of the label (e.g. 4:1 = 4.0). */
    var targetRatio: Float = (8.5f / 3.625f)
    var ratioTolerance: Float = 0.1f
    var outputWidth: Int = 800
    var outputHeight: Int = 200
    /** Percent height of tallest line to keep, 0-1 */
    var lineHeightPercent: Float = 0.75f
}

