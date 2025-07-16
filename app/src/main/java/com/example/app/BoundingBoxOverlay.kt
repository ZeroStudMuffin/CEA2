package com.example.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Overlay view drawing a fixed landscape bounding box with orientation text.
 */
class BoundingBoxOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

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
        rect.set(calculateBoxRect(w, h))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Reason: avoid drawing outside the view bounds which triggered
        // "out of bounds transparent region" messages in logcat
        canvas.clipRect(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRect(rect, boxPaint)
        canvas.drawText("TOP", rect.centerX(), rect.top - 10f, textPaint)
    }

    /**
     * Returns the crop rectangle in view coordinates.
     */
    fun getCropRect(): RectF = RectF(rect)

    /**
     * Maps the crop rectangle to bitmap coordinates.
     */
    fun mapToBitmapRect(bitmapWidth: Int, bitmapHeight: Int): Rect {
        return scaleRect(rect, width, height, bitmapWidth, bitmapHeight)
    }

    /**
     * Returns the current crop box aspect ratio (width / height).
     */
    fun aspectRatio(): Float = rect.width() / rect.height()

    companion object {
        /**
         * Calculates the bounding box rectangle for the given view size.
         * The box spans 60% of the view's width while maintaining a 34:15
         * aspect ratio.
         */
        fun calculateBoxRect(viewWidth: Int, viewHeight: Int): RectF {
            val boxWidth = 0.6f * viewWidth
            val boxHeight = boxWidth * 15f / 34f
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
