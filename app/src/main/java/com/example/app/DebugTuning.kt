package com.example.app

/** Holds adjustable debug tuning options for the OCR pipeline. */
data class TuningOptions(
    var heightPercent: Float = 0.75f,
    var binarizeThreshold: Int = 128,
    var blurRadius: Int = 0,
    var contrast: Float = 1f,
    var brightness: Float = 1f,
    var sharpenSigma: Float = 0f,
    var expandCropPercent: Float = 0f,
    var rotateThreshold: Float = 0f
)

/** Singleton storing current tuning values. */
object DebugTuning {
    var options: TuningOptions = TuningOptions()

    /** Reset all tuning values to defaults. */
    fun reset() {
        options = TuningOptions()
    }
}
