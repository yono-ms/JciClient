package com.example.jciclient.view

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Point
import android.util.AttributeSet
import android.view.SurfaceView
import android.view.WindowManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CustomSurfaceView : SurfaceView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    val logger: Logger by lazy { LoggerFactory.getLogger(javaClass.simpleName) }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = Point().also {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                context.display
            } else {
                @Suppress("DEPRECATION")
                (context.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
            }?.getRealSize(it)
        }
        val width = size.x
        val height = size.y
        logger.info("onSizeChanged $w $h ($oldw $oldh) $width $height")
    }
}
