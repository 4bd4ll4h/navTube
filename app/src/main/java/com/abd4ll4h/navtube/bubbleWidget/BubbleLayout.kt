package com.abd4ll4h.navtube.bubbleWidget

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import com.facebook.rebound.Spring
import kotlinx.coroutines.Runnable
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


class BubbleLayout(val windowManager: WindowManager, val context1: Context) : FrameLayout(context1),
    View.OnTouchListener {
    companion object {

        val CHAT_HEAD_SIZE: Int = dpToPx(60f)
        val CHAT_HEAD_OUT_OF_SCREEN_X: Int = dpToPx(10f)
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300
        private const val LONG_PRESS_DELAY: Long = 400
        const val CHAT_HEAD_DRAG_TOLERANCE: Float = 20f
        val CLOSE_SIZE = dpToPx(64f)
        val CLOSE_CAPTURE_DISTANCE = dpToPx(100f)
        val CLOSE_CAPTURE_DISTANCE_THROWN = dpToPx(300f)
        val CLOSE_ADDITIONAL_SIZE = dpToPx(24f)


        fun distance(x1: Float, x2: Float, y1: Float, y2: Float): Float {
            return ((x1 - x2).pow(2) + (y1 - y2).pow(2))
        }

    }


    private var initialX = 0.0f
    private var initialY = 0.0f
    private var moving = false
    private var initialTouchX = 0.0f
    private var initialTouchY = 0.0f
    var initialVelocityX = 0.0
    var initialVelocityY = 0.0
    private var lastY = 0.0
    private var velocityTracker: VelocityTracker? = null
    var closeCaptured = false
    private var closeVelocityCaptured = false
    private var isOnRight = true
    private var detectedOutOfBounds = false
    private var clickRunnable: Runnable? = null
    private var closeRunnable: Runnable? = null

    private var movingOutOfClose = false
    private var isDoubleClick = false
    private var isLongPressed = false
    val metrics = getScreenSize()
    var wasMoving = false
    var motionTracker = LinearLayout(context1)
     var close = Close(this)
    var content = Content(context)
    var lastClickTime: Long = 0
     var motionTrackerParams = WindowManager.LayoutParams(
        CHAT_HEAD_SIZE,
        CHAT_HEAD_SIZE + 16,
        getOverlayFlag(),
        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS and
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv(),
        PixelFormat.TRANSLUCENT
    )

    private var containerParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        getOverlayFlag(),
        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        PixelFormat.TRANSLUCENT
    )

    val bubbleLayoutContent = Bubble(this)

    init {

        containerParams.gravity = Gravity.TOP or Gravity.START
        motionTrackerParams.gravity = Gravity.TOP or Gravity.START
        containerParams.dimAmount = 0.7f

        this.clipToPadding = false
        BubbleService.instance.windowManager.addView(this, containerParams)

        //motionTracker.setBackgroundColor(Color.BLACK)
        BubbleService.instance.windowManager.addView(motionTracker, motionTrackerParams)
        this.addView(content,FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT))

        postDelayed({
            motionTrackerParams.x=bubbleLayoutContent.x.toInt()
            motionTrackerParams.y=bubbleLayoutContent.y.toInt()
            BubbleService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)
                    },500)


        motionTracker.setOnTouchListener(this)
        setOnTouchListener { v, event ->
            v.performClick()

            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if (v == this) {
                        collapse()
                    }
                }
            }

            return@setOnTouchListener false
        }


    }

    fun collapse() {


        val metrics = getScreenSize()


        val newX = initialX.toDouble()
        val newY = initialY.toDouble()

        content.hideContent()


        postDelayed({
            bubbleLayoutContent.hideContent()
            bubbleLayoutContent.springX.endValue = newX
            bubbleLayoutContent.springY.endValue = newY
        },200)

        motionTrackerParams.flags =
            motionTrackerParams.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        BubbleService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)

        containerParams.flags =
            (containerParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) and
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv() and
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL.inv() or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

        BubbleService.instance.windowManager.updateViewLayout(this, containerParams)
    }

    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {


        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                clickRunnable?.let { handler.removeCallbacks(it) }
                clickRunnable = Runnable {
                    isLongPressed = true
                    onLongClick()

                }
                handler.postDelayed(clickRunnable!!, LONG_PRESS_DELAY)
                initialX = bubbleLayoutContent.springX.currentValue.toFloat()
                initialY = bubbleLayoutContent.springY.currentValue.toFloat()
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                bubbleLayoutContent.springX.setAtRest()
                bubbleLayoutContent.springY.setAtRest()
                detectedOutOfBounds = false
                closeVelocityCaptured = false

                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain()
                } else {
                    velocityTracker?.clear()
                }

                velocityTracker?.addMovement(event)


                wasMoving = false
                moving = false

                closeRunnable?.let { handler.removeCallbacks(it) }
                closeRunnable=Runnable{
                    close.show()
                }
                handler.postDelayed(closeRunnable!!, 100)

            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(event)
                if (distance(
                        initialTouchX,
                        event.rawX,
                        initialTouchY,
                        event.rawY
                    ) > CHAT_HEAD_DRAG_TOLERANCE.pow(2)
                ) {
                    moving = true
                    clickRunnable?.let { handler.removeCallbacks(it) }
                }
                if (moving) {

                    close.springX.endValue =
                        (metrics.widthPixels / 2) + (((event.rawX + bubbleLayoutContent.width / 2) / 7) - metrics.widthPixels / 2 / 7) - close.width.toDouble() / 2
                    close.springY.endValue = (metrics.heightPixels - CLOSE_SIZE) + max(
                        ((event.rawY + close.height / 2) / 10) - metrics.heightPixels / 10,
                        -dpToPx(30f).toFloat()
                    ) - dpToPx(60f).toDouble()

                    if (distance(
                            close.springX.endValue.toFloat() + close.width / 2,
                            event.rawX,
                            close.springY.endValue.toFloat() + close.height / 2,
                            event.rawY
                        ) < CLOSE_CAPTURE_DISTANCE.toDouble().pow(2)
                    ) {
                        bubbleLayoutContent.springX.springConfig = SpringConfigs.CAPTURING
                        bubbleLayoutContent.springY.springConfig = SpringConfigs.CAPTURING

                        close.enlarge()
                        closeCaptured = true
                    } else if (closeCaptured) {
                        bubbleLayoutContent.springX.springConfig = SpringConfigs.CAPTURING
                        bubbleLayoutContent.springY.springConfig = SpringConfigs.CAPTURING

                        close.resetScale()

                        bubbleLayoutContent.springX.endValue =
                            initialX + (event.rawX - initialTouchX).toDouble()
                        bubbleLayoutContent.springY.endValue =
                            initialY + (event.rawY - initialTouchY).toDouble()

                        closeCaptured = false

                        movingOutOfClose = true

                        postDelayed({
                            movingOutOfClose = false
                        }, 100)
                    } else if (!movingOutOfClose) {
                        bubbleLayoutContent.springX.springConfig = SpringConfigs.DRAGGING
                        bubbleLayoutContent.springY.springConfig = SpringConfigs.DRAGGING

                        bubbleLayoutContent.springX.currentValue =
                            initialX + (event.rawX - initialTouchX).toDouble()
                        bubbleLayoutContent.springY.currentValue =
                            initialY + (event.rawY - initialTouchY).toDouble()

                        velocityTracker?.computeCurrentVelocity(1000)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {

                if (moving) wasMoving = true

                closeRunnable?.let { handler.removeCallbacks(it) }
                closeRunnable=Runnable{
                    close.hide()
                }
                handler.postDelayed(closeRunnable!!, 100)

                if (closeCaptured) {
                    onClose()
                    return true
                }
                if (!moving) {
                    if (!isLongPressed) {
                        val clickTime = System.currentTimeMillis()
                        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {

                            clickRunnable?.let { handler.removeCallbacks(it) }

                            clickRunnable = Runnable {
                                onDoubleClick()
                            }
                            handler.postDelayed(clickRunnable!!, DOUBLE_CLICK_TIME_DELTA)
                        } else {
                            clickRunnable?.let { handler.removeCallbacks(it) }

                            clickRunnable = Runnable {
                                bubbleLayoutContent.headOnClick()
                            }
                            handler.postDelayed(clickRunnable!!, DOUBLE_CLICK_TIME_DELTA)
                        }

                        lastClickTime = clickTime
                    }
                    isLongPressed = false
                } else {
                    moving = false

                    var xVelocity = velocityTracker!!.xVelocity.toDouble()
                    var yVelocity = velocityTracker!!.yVelocity.toDouble()
                    var maxVelocityX = 0.0
                    velocityTracker?.recycle()
                    velocityTracker = null



                    if (xVelocity < -3500) {

                        val newVelocity =
                            ((-bubbleLayoutContent.springX.currentValue - CHAT_HEAD_OUT_OF_SCREEN_X) * SpringConfigs.DRAGGING.friction)
                        maxVelocityX = newVelocity - 5000
                        if (xVelocity > maxVelocityX)
                            xVelocity = newVelocity - 500
                    } else if (xVelocity > 3500) {

                        val newVelocity =
                            ((metrics.widthPixels - bubbleLayoutContent.springX.currentValue - bubbleLayoutContent.width + CHAT_HEAD_OUT_OF_SCREEN_X) * SpringConfigs.DRAGGING.friction)
                        maxVelocityX = newVelocity + 5000
                        if (maxVelocityX > xVelocity)
                            xVelocity = newVelocity + 500
                    } else if (yVelocity > 20 || yVelocity < -20) {
                        bubbleLayoutContent.springX.springConfig = SpringConfigs.NOT_DRAGGING

                        if (bubbleLayoutContent.x >= metrics.widthPixels / 2) {


                            bubbleLayoutContent.springX.endValue =
                                metrics.widthPixels - bubbleLayoutContent.width + CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                            isOnRight = true
                        } else {
                            bubbleLayoutContent.springX.endValue =
                                -CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()

                            isOnRight = false
                        }
                    } else {
                        bubbleLayoutContent.springX.springConfig = SpringConfigs.NOT_DRAGGING
                        bubbleLayoutContent.springY.springConfig = SpringConfigs.NOT_DRAGGING

                        if (bubbleLayoutContent.x >= metrics.widthPixels / 2) {
                            isOnRight = true

                            bubbleLayoutContent.springX.endValue =
                                metrics.widthPixels - bubbleLayoutContent.width + CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                            bubbleLayoutContent.springY.endValue = bubbleLayoutContent.y.toDouble()


                        } else {

                            isOnRight = false
                            bubbleLayoutContent.springX.endValue =
                                -CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                            bubbleLayoutContent.springY.endValue = bubbleLayoutContent.y.toDouble()


                        }
                    }


                    xVelocity = if (xVelocity < 0) {
                        max(xVelocity - 1000.0, maxVelocityX)
                    } else {
                        min(xVelocity + 1000.0, maxVelocityX)
                    }

                    initialVelocityX = xVelocity
                    initialVelocityY = yVelocity

                    bubbleLayoutContent.springX.velocity = xVelocity
                    bubbleLayoutContent.springY.velocity = yVelocity
                }

            }
        }

        return true
    }

    private fun onLongClick() {
        val metrics = getScreenSize()

        motionTrackerParams.flags =
            motionTrackerParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        BubbleService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)
        containerParams.flags =
            (containerParams.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()) or WindowManager.LayoutParams.FLAG_DIM_BEHIND and WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL.inv() and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        BubbleService.instance.windowManager.updateViewLayout(this, containerParams)

        bubbleLayoutContent.springX.springConfig = SpringConfigs.NOT_DRAGGING
        bubbleLayoutContent.springY.springConfig = SpringConfigs.NOT_DRAGGING
        val x = metrics.widthPixels - bubbleLayoutContent.width.toDouble()*2 - dpToPx(16f)
        val y = metrics.heightPixels - bubbleLayoutContent.height.toDouble() - dpToPx(24f)
        bubbleLayoutContent.springX.endValue = x
        bubbleLayoutContent.springY.endValue = y
        handler.postDelayed({
            bubbleLayoutContent.showContent()
            content.showContent()
        }, 200)
        Toast.makeText(
            context,
            "long click w= " + bubbleLayoutContent.measuredWidth,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun onDoubleClick() {
        Toast.makeText(context, "double click", Toast.LENGTH_SHORT).show()
    }

    fun onChatHeadSpringUpdate(spring: Spring, totalVelocity: Int) {

        Log.i("@double","scW="+bubbleLayoutContent.springX.endValue+" CW="+bubbleLayoutContent.height)
        content.x = (metrics.widthPixels-content.width- dpToPx(16f)).toFloat()
        content.y = (bubbleLayoutContent.springY.endValue- content.height-dpToPx(16f)).toFloat()
        content.pivotX = abs(bubbleLayoutContent.x+bubbleLayoutContent.width/2)
        content.pivotY =bubbleLayoutContent.y-content.height- dpToPx(24f)


        val width = dpToPx(100f)
        val height = dpToPx(50f)
        val r1 = Rectangle(
            close.x.toDouble() - if (isOnRight) dpToPx(32f) else width,
            close.y.toDouble() - height / 2,
            close.width.toDouble() + width,
            close.height.toDouble() + height
        )

        val x = bubbleLayoutContent.springX.currentValue + bubbleLayoutContent.params.width / 2
        val y = bubbleLayoutContent.springY.currentValue + bubbleLayoutContent.params.height / 2

        val l1 = Line(x, y, initialVelocityX + x, initialVelocityY + y)
        if (
            !moving &&
            initialVelocityY > 5000.0 &&
            l1.intersects(r1) &&
            close.visibility == VISIBLE &&
            !closeVelocityCaptured
        ) {
            closeVelocityCaptured = true

            bubbleLayoutContent.springX.endValue =
                close.springX.endValue + close.width / 2 - bubbleLayoutContent.params.width / 2 + 2
            bubbleLayoutContent.springY.endValue =
                close.springY.endValue + close.height / 2 - bubbleLayoutContent.params.height / 2 + 2

            close.enlarge()

            postDelayed({
                onClose()
            }, 100)
        }
        if (wasMoving) {
            motionTrackerParams.x =
                if (isOnRight) metrics.widthPixels - bubbleLayoutContent.width else 0

            lastY = bubbleLayoutContent.springY.currentValue

            if (!detectedOutOfBounds && !closeCaptured && !closeVelocityCaptured) {
                if (bubbleLayoutContent.springY.currentValue < 0) {
                    bubbleLayoutContent.springY.endValue = 0.0
                    detectedOutOfBounds = true
                } else if (bubbleLayoutContent.springY.currentValue > metrics.heightPixels) {
                    bubbleLayoutContent.springY.endValue =
                        metrics.heightPixels - CHAT_HEAD_SIZE.toDouble()
                    detectedOutOfBounds = true
                }
            }

            if (!moving) {
                if (spring === bubbleLayoutContent.springX) {
                    val xPosition = bubbleLayoutContent.springX.currentValue
                    if (xPosition + bubbleLayoutContent.width > metrics.widthPixels && bubbleLayoutContent.springX.velocity > 0) {
                        val newPos =
                            metrics.widthPixels - bubbleLayoutContent.width + CHAT_HEAD_OUT_OF_SCREEN_X
                        bubbleLayoutContent.springX.springConfig = SpringConfigs.NOT_DRAGGING
                        bubbleLayoutContent.springX.endValue = newPos.toDouble()
                        isOnRight = true
                    } else if (xPosition < 0 && bubbleLayoutContent.springX.velocity < 0) {
                        bubbleLayoutContent.springX.springConfig = SpringConfigs.NOT_DRAGGING
                        bubbleLayoutContent.springX.endValue = -CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                        isOnRight = false
                    }
                } else if (spring === bubbleLayoutContent.springY) {
                    val yPosition = bubbleLayoutContent.springY.currentValue
                    if (yPosition + bubbleLayoutContent.height > metrics.heightPixels && bubbleLayoutContent.springY.velocity > 0) {
                        bubbleLayoutContent.springY.springConfig = SpringConfigs.NOT_DRAGGING
                        bubbleLayoutContent.springY.endValue =
                            metrics.heightPixels - bubbleLayoutContent.height.toDouble() - dpToPx(
                                25f
                            )
                    } else if (yPosition < 0 && bubbleLayoutContent.springY.velocity < 0) {
                        bubbleLayoutContent.springY.springConfig = SpringConfigs.NOT_DRAGGING
                        bubbleLayoutContent.springY.endValue = 0.0
                    }
                }
            }

            if (abs(totalVelocity) % 10 == 0 && !moving && bubbleLayoutContent != null) {
                motionTrackerParams.y = bubbleLayoutContent.springY.currentValue.toInt()

                Log.i("@total", "tatal vl")
                BubbleService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)
            }
        }
    }

    private fun onClose() {
        this.removeAllViews()
        BubbleService.instance.windowManager.removeView(this)
        BubbleService.instance.windowManager.removeView(motionTracker)
        closeCaptured = false
        movingOutOfClose = false
    }


}
