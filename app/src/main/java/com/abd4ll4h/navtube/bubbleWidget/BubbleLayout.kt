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
        private const val LONG_PRESS_DELAY: Long = 500
        const val CHAT_HEAD_DRAG_TOLERANCE: Float = 20f
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

    private var isDoubleClick = false
    private var isLongPressed = false
    val metrics = getScreenSize()
    private var wasMoving = false
    private var motionTracker = LinearLayout(context1)
    var lastClickTime: Long = 0
    private var motionTrackerParams = WindowManager.LayoutParams(
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

    val bubbleLayoutContent = BubbleLayoutContent(this)

    init {

        containerParams.gravity = Gravity.TOP or Gravity.START
        motionTrackerParams.gravity = Gravity.TOP or Gravity.START
        containerParams.dimAmount = 0.7f

        windowManager.addView(this, containerParams)
        motionTrackerParams.x = bubbleLayoutContent.x.toInt()
        motionTrackerParams.y = bubbleLayoutContent.y.toInt()

        //motionTracker.setBackgroundColor(Color.BLACK)
        windowManager.addView(motionTracker, motionTrackerParams)


        Log.i("check@bubble", "pX=" + motionTrackerParams.x + " bX=" + bubbleLayoutContent.x)

        motionTracker.setOnTouchListener(this)


    }

    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {


        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                clickRunnable?.let { handler.removeCallbacks(it) }
                clickRunnable= Runnable {
                    isLongPressed=true
                    onLongClick()

                }
                handler.postDelayed(clickRunnable!!,LONG_PRESS_DELAY)
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
                Log.i("@double","y V="+velocityTracker!!.yVelocity+" x V="+velocityTracker!!.xVelocity)

                wasMoving = false
                moving = false
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(event)
                if (distance(initialTouchX, event.rawX, initialTouchY, event.rawY) > CHAT_HEAD_DRAG_TOLERANCE.pow(2)) {
                    moving = true
                    clickRunnable?.let { handler.removeCallbacks(it) }
                }
                if (moving) {
                    bubbleLayoutContent.springX.springConfig = SpringConfigs.DRAGGING
                    bubbleLayoutContent.springY.springConfig = SpringConfigs.DRAGGING

                    bubbleLayoutContent.springX.currentValue =
                        initialX + (event.rawX - initialTouchX).toDouble()
                    bubbleLayoutContent.springY.currentValue =
                        initialY + (event.rawY - initialTouchY).toDouble()
                }
                velocityTracker?.computeCurrentVelocity(1000)
            }
            MotionEvent.ACTION_UP -> {

                if (moving) wasMoving = true
                if (!moving) {

                    if(!isLongPressed){
                        val clickTime = System.currentTimeMillis()
                        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {

                            clickRunnable?.let { handler.removeCallbacks(it) }

                            clickRunnable = Runnable {
                                onDoubleClick()
                            }
                            handler.postDelayed(clickRunnable!!, DOUBLE_CLICK_TIME_DELTA)
                        }else{
                            clickRunnable?.let { handler.removeCallbacks(it) }

                            clickRunnable = Runnable {
                                bubbleLayoutContent.headOnClick()
                            }
                            handler.postDelayed(clickRunnable!!, DOUBLE_CLICK_TIME_DELTA)
                        }

                        lastClickTime = clickTime
                    }
                    isLongPressed=false
                } else {
                    moving = false

                    var xVelocity = velocityTracker!!.xVelocity.toDouble()
                    var yVelocity = velocityTracker!!.yVelocity.toDouble()
                    var maxVelocityX = 0.0
                    velocityTracker?.recycle()
                    velocityTracker = null



                    if (xVelocity < -3500) {

                        val newVelocity = ((-bubbleLayoutContent.springX.currentValue -  CHAT_HEAD_OUT_OF_SCREEN_X) * SpringConfigs.DRAGGING.friction)
                        maxVelocityX = newVelocity - 5000
                        if (xVelocity > maxVelocityX)
                            xVelocity = newVelocity - 500
                    }
                    else if (xVelocity > 3500) {

                        val newVelocity = ((metrics.widthPixels - bubbleLayoutContent.springX.currentValue - bubbleLayoutContent.width + CHAT_HEAD_OUT_OF_SCREEN_X) * SpringConfigs.DRAGGING.friction)
                        maxVelocityX = newVelocity + 5000
                        if (maxVelocityX > xVelocity)
                            xVelocity = newVelocity + 500
                    }
                    else if (yVelocity > 20 || yVelocity < -20) {
                        bubbleLayoutContent.springX.springConfig = SpringConfigs.NOT_DRAGGING

                        if (bubbleLayoutContent.x >= metrics.widthPixels / 2) {


                            bubbleLayoutContent.springX.endValue = metrics.widthPixels - bubbleLayoutContent.width + CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                            isOnRight = true
                        } else {
                            bubbleLayoutContent.springX.endValue = -CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()

                            isOnRight = false
                        }
                    }
                    else {
                        bubbleLayoutContent.springX.springConfig = SpringConfigs.NOT_DRAGGING
                        bubbleLayoutContent.springY.springConfig = SpringConfigs.NOT_DRAGGING

                        if (bubbleLayoutContent.x >= metrics.widthPixels / 2) {
                            isOnRight = true

                            bubbleLayoutContent.springX.endValue = metrics.widthPixels - bubbleLayoutContent.width + CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                            bubbleLayoutContent.springY.endValue = bubbleLayoutContent.y.toDouble()


                        } else {

                            isOnRight = false
                            bubbleLayoutContent.springX.endValue = -CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
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
        Toast.makeText(context,"long click",Toast.LENGTH_SHORT).show()
    }

    private fun onDoubleClick() {
        Toast.makeText(context,"double click",Toast.LENGTH_SHORT).show()
    }

    fun onChatHeadSpringUpdate(spring: Spring, totalVelocity: Int) {
        if (wasMoving) {
            motionTrackerParams.x = if (isOnRight) metrics.widthPixels - bubbleLayoutContent.width else 0

            lastY = bubbleLayoutContent.springY.currentValue

            if (!detectedOutOfBounds && !closeCaptured && !closeVelocityCaptured) {
                if (bubbleLayoutContent.springY.currentValue < 0) {
                    bubbleLayoutContent.springY.endValue = 0.0
                    detectedOutOfBounds = true
                } else if (bubbleLayoutContent.springY.currentValue > metrics.heightPixels) {
                    bubbleLayoutContent.springY.endValue = metrics.heightPixels - CHAT_HEAD_SIZE.toDouble()
                    detectedOutOfBounds = true
                }
            }

            if (!moving) {
                if (spring === bubbleLayoutContent.springX) {
                    val xPosition = bubbleLayoutContent.springX.currentValue
                    if (xPosition + bubbleLayoutContent.width > metrics.widthPixels && bubbleLayoutContent.springX.velocity > 0) {
                        val newPos = metrics.widthPixels - bubbleLayoutContent.width + CHAT_HEAD_OUT_OF_SCREEN_X
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
                        bubbleLayoutContent.springY.endValue = metrics.heightPixels - bubbleLayoutContent.height.toDouble() - dpToPx(25f)
                    } else if (yPosition < 0 && bubbleLayoutContent.springY.velocity < 0) {
                        bubbleLayoutContent.springY.springConfig = SpringConfigs.NOT_DRAGGING
                        bubbleLayoutContent.springY.endValue = 0.0
                    }
                }
            }

            if (abs(totalVelocity) % 10 == 0 && !moving && bubbleLayoutContent != null) {
                motionTrackerParams.y = bubbleLayoutContent.springY.currentValue.toInt()

                Log.i("@total","tatal vl" )
                windowManager.updateViewLayout(motionTracker, motionTrackerParams)
            }
        }
    }




}
