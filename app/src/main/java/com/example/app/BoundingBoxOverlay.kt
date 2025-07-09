package com.example.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

/**
 * Overlay view drawing a fixed landscape bounding box with orientation text.
 */
class BoundingBoxOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    /** Current rotation applied to the overlay in degrees. */
    var rotationDegrees: Int = 0

    /**
     * Applies a new rotation to the overlay and recalculates the bounding box.
     */
    fun applyRotation(degrees: Int) {
        rotationDegrees = degrees % 360
        rect.set(calculateBoxRect(width, height, rotationDegrees))
        invalidate()
    }

    private val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.GREEN
        textSize = 48f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val rect = RectF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rect.set(calculateBoxRect(w, h, rotationDegrees))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.rotate(rotationDegrees.toFloat(), width / 2f, height / 2f)
        canvas.drawRect(rect, boxPaint)
        canvas.drawText("TOP", rect.centerX(), rect.top - 10f, textPaint)
        canvas.restore()
    }

    /**
     * Returns the crop rectangle in view coordinates.
     */
    fun getCropRect(): RectF {
        val result = RectF(rect)
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat(), width / 2f, height / 2f) }
        matrix.mapRect(result)
        return result
    }

    /**
     * Maps the crop rectangle to bitmap coordinates.
     */
    fun mapToBitmapRect(bitmapWidth: Int, bitmapHeight: Int): Rect {
        val rotated = getCropRect()
        return scaleRect(rotated, width, height, bitmapWidth, bitmapHeight)
    }

    companion object {
        /**
         * Calculates the bounding box rectangle for the given view size.
         */
        fun calculateBoxRect(viewWidth: Int, viewHeight: Int, rotationDegrees: Int = 0): RectF {
            val boxSize = 0.7f * min(viewWidth, viewHeight)
            val ratio = 15f / 34f
            val landscape = rotationDegrees % 180 == 0
            val boxWidth = if (landscape) boxSize else boxSize * ratio
            val boxHeight = if (landscape) boxSize * ratio else boxSize
            val left = (viewWidth - boxWidth) / 2f
            val top = (viewHeight - boxHeight) / 2f
            return RectF(left, top, left + boxWidth, top + boxHeight)
        }

        /**
         * Scales a rectangle from view coordinates to bitmap coordinates.
         */
        fun scaleRect(
            rect: RectF,
            viewWidth: Int,
            viewHeight: Int,
            bitmapWidth: Int,
            bitmapHeight: Int
        ): Rect {
            val scaleX = bitmapWidth.toFloat() / viewWidth
            val scaleY = bitmapHeight.toFloat() / viewHeight
            return Rect(
                (rect.left * scaleX).toInt(),
                (rect.top * scaleY).toInt(),
                (rect.right * scaleX).toInt(),
                (rect.bottom * scaleY).toInt()
            )
        }
    }
}
