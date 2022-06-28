package com.abd4ll4h.navtube.bubbleWidget

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.abd4ll4h.navtube.R
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringConfig
import com.facebook.rebound.SpringListener
import com.facebook.rebound.SpringSystem


/**
 * Created by karthikrk on 12/09/15.
 */
class ChatHead : View, SpringListener {
    var mXSprings: Spring? = null
    var mYSprings: Spring? = null
    private val mPaint = Paint()
    var mBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_round)

    constructor(context: Context?) : super(context) {
        intialize()
    }

    constructor(context: Context?, attributeSet: AttributeSet?) : super(context, attributeSet) {
        intialize()
    }

    constructor(context: Context?, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
        intialize()
    }

    private fun intialize() {
        val ss = SpringSystem.create()
        var s: Spring
        s = ss.createSpring()
        s.springConfig = MySpringConfig(200.0, if (0 == 0) 8.0 else (15 + 0 * 2).toDouble(), 0, true)
        s.addListener(this)
        mXSprings = s
        s = ss.createSpring()
        s.springConfig = MySpringConfig(200.0, if (0 == 0) 8.0 else (15 + 0 * 2).toDouble(), 0, false)
        s.addListener(this)
        mYSprings = s
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mXSprings!!.currentValue = (w / 2).toDouble()
        mYSprings!!.currentValue = 0.0
        mXSprings!!.endValue = (w / 2).toDouble()
        mYSprings!!.endValue = (h / 2).toDouble()
    }

    override fun onSpringActivate(s: Spring) {}
    override fun onSpringAtRest(s: Spring) {}
    override fun onSpringEndStateChange(s: Spring) {}
    override fun onSpringUpdate(s: Spring) {
        val cfg = s.springConfig as MySpringConfig
        if (cfg.index < NUM_ELEMS - 1) {
            val springs = if (cfg.horizontal) mXSprings else mYSprings
            springs!!.endValue = s.currentValue
        }
        if (cfg.index == 0) {
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        for (i in NUM_ELEMS - 1 downTo 0) {
            mPaint.alpha = if (i == 0) 255 else 192 - i * 128 / NUM_ELEMS
            canvas.drawBitmap(
                mBitmap,
                mXSprings!!.currentValue.toFloat() - mBitmap.width / 2,
                mYSprings!!.currentValue.toFloat() - mBitmap.height / 2,
                mPaint
            )
        }
    }

    internal inner class MySpringConfig(
        tension: Double,
        friction: Double,
        var index: Int,
        var horizontal: Boolean
    ) :
        SpringConfig(tension, friction)

    companion object {
        private const val NUM_ELEMS = 1
    }
}