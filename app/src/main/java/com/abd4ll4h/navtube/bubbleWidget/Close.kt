package com.abd4ll4h.navtube.bubbleWidget

import android.graphics.*
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.abd4ll4h.navtube.R
import com.abd4ll4h.navtube.bubbleWidget.BubbleLayout.Companion.CLOSE_ADDITIONAL_SIZE
import com.abd4ll4h.navtube.bubbleWidget.BubbleLayout.Companion.CLOSE_SIZE
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringSystem

class Close(var bubble: BubbleLayout): View(bubble.context) {
    private var params = WindowManager.LayoutParams(
        CLOSE_SIZE + CLOSE_ADDITIONAL_SIZE,
        CLOSE_SIZE + CLOSE_ADDITIONAL_SIZE,
        getOverlayFlag(),
        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        PixelFormat.TRANSLUCENT
    )

    private var gradientParams = FrameLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dpToPx(150f))

    var springSystem = SpringSystem.create()

    var springY = springSystem.createSpring()
    var springX = springSystem.createSpring()
    var springAlpha = springSystem.createSpring()
    var springScale = springSystem.createSpring()

    val paint = Paint()

    val gradient = FrameLayout(context)

    var hidden = true

    private var bitmapBg = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.close_bg), CLOSE_SIZE, CLOSE_SIZE, false)!!
    private val bitmapClose = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.close), dpToPx(28f), dpToPx(28f), false)!!

    fun hide() {
        val metrics = getScreenSize()
        springY.endValue = metrics.heightPixels.toDouble() + height
        springX.endValue = metrics.widthPixels.toDouble() / 2 - width / 2

        springAlpha.endValue = 0.0
        hidden = true
    }

    fun show() {
        hidden = false
        visibility = View.VISIBLE

        springAlpha.endValue = 1.0
        resetScale()
    }

    fun enlarge() {
        springScale.endValue = CLOSE_ADDITIONAL_SIZE.toDouble()
    }

    fun resetScale() {
        springScale.endValue = 1.0
    }

    fun onPositionUpdate() {
        if (bubble.closeCaptured && bubble.bubble != null) {
            bubble.bubble.springX.endValue = springX.endValue + width / 2 - bubble.bubble.params.width / 2 + 2
            bubble.bubble.springY.endValue = springY.endValue + height / 2 - bubble.bubble.params.height / 2 + 2
        }
    }

    init {
        this.setLayerType(View.LAYER_TYPE_HARDWARE, paint)

        visibility = View.INVISIBLE
        hide()

        springY.addListener(object : SimpleSpringListener() {
            override fun onSpringUpdate(spring: Spring) {
                y = spring.currentValue.toFloat()

                if (bubble.closeCaptured && bubble.wasMoving && bubble.bubble != null) {
                    bubble.bubble.springY.currentValue = spring.currentValue
                }

                onPositionUpdate()
            }
        })

        springX.addListener(object : SimpleSpringListener() {
            override fun onSpringUpdate(spring: Spring) {
                x = spring.currentValue.toFloat()

                onPositionUpdate()
            }
        })

        springScale.addListener(object : SimpleSpringListener() {
            override fun onSpringUpdate(spring: Spring) {
                bitmapBg =  Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.close_bg), (spring.currentValue + CLOSE_SIZE).toInt(), (spring.currentValue + CLOSE_SIZE).toInt(), false)
                invalidate()
            }
        })

        springAlpha.addListener(object : SimpleSpringListener() {
            override fun onSpringUpdate(spring: Spring) {
                gradient.alpha = spring.currentValue.toFloat()
            }
        })

        springScale.springConfig = SpringConfigs.CLOSE_SCALE
        springY.springConfig = SpringConfigs.CLOSE_Y

        params.gravity = Gravity.START or Gravity.TOP
        gradientParams.gravity = Gravity.BOTTOM

        gradient.background = ContextCompat.getDrawable(context, R.drawable.gradient_bg)
        springAlpha.currentValue = 0.0

        z = 100f

        bubble.addView(this, params)
        bubble.addView(gradient, gradientParams)
    }


    override fun onDraw(canvas: Canvas?) {
        canvas?.drawBitmap(bitmapBg, width / 2 - bitmapBg.width.toFloat() / 2, height / 2 - bitmapBg.height.toFloat() / 2, paint)
        canvas?.drawBitmap(bitmapClose, width / 2 - bitmapClose.width.toFloat() / 2, height / 2 - bitmapClose.height.toFloat() / 2, paint)
    }
}