package com.example.jciclient.view

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CustomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    companion object {
        const val SCALE_MAX = 3.0F
        const val SCALE_MIN = 0.5F
        const val PINCH_SENSITIVITY = 5.0F
    }

    val logger: Logger by lazy { LoggerFactory.getLogger(javaClass.simpleName) }

    var currentMatrix = Matrix()

    private val scaleGestureDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            var focusX: Float = 0F
            var focusY: Float = 0F

            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                logger.info("onScale")
                if (detector == null) {
                    return super.onScale(detector)
                }
                val previousScale = getMatrixValue(Matrix.MSCALE_Y)

                val scaleFactor = if (detector.scaleFactor >= 1.0F) {
                    1 + (detector.scaleFactor - 1) / (previousScale * PINCH_SENSITIVITY)
                } else {
                    1 - (1 - detector.scaleFactor) / (previousScale * PINCH_SENSITIVITY)
                }
                val scale = scaleFactor * previousScale
                if (scale < SCALE_MIN) {
                    return false
                }
                if (scale > SCALE_MAX) {
                    return false
                }
                logger.info("scaleFactor=$scaleFactor focusX=$focusX focusY=$focusY")
                currentMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY)
                invalidate()
                return super.onScale(detector)
            }

            override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                logger.info("onScaleBegin")
                detector?.also {
                    focusX = it.focusX
                    focusY = it.focusY
                }
                return super.onScaleBegin(detector)
            }

            override fun onScaleEnd(detector: ScaleGestureDetector?) {
                logger.info("onScaleEnd")
                super.onScaleEnd(detector)
            }

        })

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                logger.info("onScroll")
                val imageWidth = drawable.intrinsicWidth * getMatrixValue(Matrix.MSCALE_X)
                val imageHeight = drawable.intrinsicHeight * getMatrixValue(Matrix.MSCALE_Y)
                val left = getMatrixValue(Matrix.MTRANS_X)
                val right = left + imageWidth
                val top = getMatrixValue(Matrix.MTRANS_Y)
                val bottom = top + imageHeight

                if (width >= imageWidth && height >= imageHeight) {
                    return false
                }

                var x = -distanceX
                var y = -distanceY

                if (width > imageWidth) {
                    x = 0F
                } else {
                    if (left > 0 && x > 0) {
                        x = -left
                    } else if (right < width && x < 0) {
                        x = width - right
                    }
                }

                if (height > imageHeight) {
                    y = 0F
                } else {
                    if (top > 0 && y > 0) {
                        y = top
                    } else if (bottom < height && y < 0) {
                        y = height - bottom
                    }
                }

                currentMatrix.postTranslate(x, y)
                invalidate()
                return super.onScroll(e1, e2, distanceX, distanceY)
            }
        })

    fun getMatrixValue(index: Int): Float {
        logger.info("getMatrixValue $index")
        val values = FloatArray(9) { 0F }
        currentMatrix.getValues(values)
        logger.info("values=$values")
        return values[index]
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        imageMatrix = currentMatrix
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    init {
        scaleType = ScaleType.MATRIX
    }
}