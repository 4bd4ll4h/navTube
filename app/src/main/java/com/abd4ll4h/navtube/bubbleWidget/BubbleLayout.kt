package com.abd4ll4h.navtube.bubbleWidget

import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.abd4ll4h.navtube.R
import com.facebook.rebound.Spring
import kotlinx.coroutines.Runnable
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


class BubbleLayout(val windowManager: WindowManager, val context1: Context) : FrameLayout(context1),
    View.OnTouchListener {
    companion object {

        val CHAT_HEAD_SIZE: Int = dpToPx(50f)
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
    var  isClose=false
    private var movingOutOfClose = false
    private var isDoubleClick = false
    private var isLongPressed = false
    val metrics = getScreenSize()
    var wasMoving = false
    var motionTracker = LinearLayout(context1)
     var close = Close(this)
    var content = Content(context1)
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

    val bubble = Bubble(this)

    init {

        containerParams.gravity = Gravity.TOP or Gravity.START
        motionTrackerParams.gravity = Gravity.TOP or Gravity.START
        containerParams.dimAmount = 0.7f

        this.clipToPadding = false
        this.clipChildren=false
        BubbleService.instance.windowManager.addView(this, containerParams)

        //motionTracker.setBackgroundColor(Color.BLACK)
        BubbleService.instance.windowManager.addView(motionTracker, motionTrackerParams)
        this.addView(content,FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT))

        postDelayed({
            motionTrackerParams.x=bubble.x.toInt()
            motionTrackerParams.y=bubble.y.toInt()
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
            bubble.hideContent()
            bubble.springX.endValue = newX
            bubble.springY.endValue = newY
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
                bubble.binding.bubbleAvatar.alpha=1f
                bubble.mScaleSpring.endValue=1.2
                clickRunnable?.let { handler.removeCallbacks(it) }
                clickRunnable = Runnable {
                    isLongPressed = true
                    onLongClick()

                }
                handler.postDelayed(clickRunnable!!, LONG_PRESS_DELAY)
                initialX = bubble.springX.currentValue.toFloat()
                initialY = bubble.springY.currentValue.toFloat()
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                bubble.springX.setAtRest()
                bubble.springY.setAtRest()
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
                        (metrics.widthPixels / 2) + (((event.rawX + bubble.width / 2) / 7) - metrics.widthPixels / 2 / 7) - close.width.toDouble() / 2
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
                        bubble.springX.springConfig = SpringConfigs.CAPTURING
                        bubble.springY.springConfig = SpringConfigs.CAPTURING

                        close.enlarge()
                        closeCaptured = true
                    } else if (closeCaptured) {
                        bubble.springX.springConfig = SpringConfigs.CAPTURING
                        bubble.springY.springConfig = SpringConfigs.CAPTURING

                        close.resetScale()

                        bubble.springX.endValue =
                            initialX + (event.rawX - initialTouchX).toDouble()
                        bubble.springY.endValue =
                            initialY + (event.rawY - initialTouchY).toDouble()

                        closeCaptured = false

                        movingOutOfClose = true

                        postDelayed({
                            movingOutOfClose = false
                        }, 100)
                    } else if (!movingOutOfClose) {
                        bubble.springX.springConfig = SpringConfigs.DRAGGING
                        bubble.springY.springConfig = SpringConfigs.DRAGGING

                        bubble.springX.currentValue =
                            initialX + (event.rawX - initialTouchX).toDouble()
                        bubble.springY.currentValue =
                            initialY + (event.rawY - initialTouchY).toDouble()

                        velocityTracker?.computeCurrentVelocity(2500)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                bubble.binding.bubbleAvatar.alpha=0.6f
                bubble.mScaleSpring.endValue=0.0
                if (moving) wasMoving = true

                closeRunnable?.let { handler.removeCallbacks(it) }
                closeRunnable=Runnable{
                    close.hide()
                }
                handler.postDelayed(closeRunnable!!, 100)

                if (closeCaptured) {
                    hideAll()
                    isClose=true
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
                                bubble.headOnClick()
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
                            ((-bubble.springX.currentValue - CHAT_HEAD_OUT_OF_SCREEN_X) * SpringConfigs.DRAGGING.friction)
                        maxVelocityX = newVelocity - 5000
                        if (xVelocity > maxVelocityX)
                            xVelocity = newVelocity - 500
                    } else if (xVelocity > 3500) {

                        val newVelocity =
                            ((metrics.widthPixels - bubble.springX.currentValue - bubble.width + CHAT_HEAD_OUT_OF_SCREEN_X) * SpringConfigs.DRAGGING.friction)
                        maxVelocityX = newVelocity + 5000
                        if (maxVelocityX > xVelocity)
                            xVelocity = newVelocity + 500
                    } else if (yVelocity > 20 || yVelocity < -20) {
                        bubble.springX.springConfig = SpringConfigs.NOT_DRAGGING

                        if (bubble.x >= metrics.widthPixels / 2) {


                            bubble.springX.endValue =
                                metrics.widthPixels - bubble.width + CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                            isOnRight = true
                        } else {
                            bubble.springX.endValue =
                                -CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()

                            isOnRight = false
                        }
                    } else {
                        bubble.springX.springConfig = SpringConfigs.NOT_DRAGGING
                        bubble.springY.springConfig = SpringConfigs.NOT_DRAGGING

                        if (bubble.x >= metrics.widthPixels / 2) {
                            isOnRight = true

                            bubble.springX.endValue =
                                metrics.widthPixels - bubble.width + CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                            bubble.springY.endValue = bubble.y.toDouble()


                        } else {

                            isOnRight = false
                            bubble.springX.endValue =
                                -CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                            bubble.springY.endValue = bubble.y.toDouble()


                        }
                    }


                    xVelocity = if (xVelocity < 0) {
                        max(xVelocity - 1000.0, maxVelocityX)
                    } else {
                        min(xVelocity + 1000.0, maxVelocityX)
                    }

                    initialVelocityX = xVelocity
                    initialVelocityY = yVelocity

                    bubble.springX.velocity = xVelocity
                    bubble.springY.velocity = yVelocity
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

        bubble.springX.springConfig = SpringConfigs.NOT_DRAGGING
        bubble.springY.springConfig = SpringConfigs.NOT_DRAGGING
        val x = metrics.widthPixels - (bubble.width.toDouble()*2.7)- dpToPx(8f)
        val y = metrics.heightPixels - bubble.height.toDouble()- dpToPx(24f)
        bubble.springX.endValue = x
        bubble.springY.endValue = y
        handler.postDelayed({
            bubble.showContent()
            content.showContent()
        }, 200)
    }

    private fun onDoubleClick() {
        if (content.onFavClicked(null)) {
            bubble.binding.bubbleAvatar.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_card_favorite, context.theme
                )
            )
            bubble.mScaleSpring.endValue = 1.2
            bubble.binding.bubbleAvatar.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_fav_checked, context.theme
                )
            )
            postDelayed({
                bubble.mScaleSpring.endValue = 0.0
                bubble.binding.bubbleAvatar.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_small_logo, context.theme
                    )
                )
            }, 1000)
        }
    }
    fun onChatHeadSpringUpdate(spring: Spring, totalVelocity: Int) {

        content.x = (metrics.widthPixels-content.width).toFloat()
        content.y = (bubble.springY.endValue- content.height).toFloat()
        content.pivotX = abs(bubble.x+bubble.width/2)
        content.pivotY =bubble.y-content.height- dpToPx(24f)


        val width = dpToPx(100f)
        val height = dpToPx(50f)
        val r1 = Rectangle(
            close.x.toDouble() - if (isOnRight) dpToPx(32f) else width,
            close.y.toDouble() - height / 2,
            close.width.toDouble() + width,
            close.height.toDouble() + height
        )

        val x = bubble.springX.currentValue + bubble.params.width / 2
        val y = bubble.springY.currentValue + bubble.params.height / 2

        val l1 = Line(x, y, initialVelocityX + x, initialVelocityY + y)
        if (
            !moving &&
            initialVelocityY > 5000.0 &&
            l1.intersects(r1) &&
            close.visibility == VISIBLE &&
            !closeVelocityCaptured
        ) {
            closeVelocityCaptured = true

            bubble.springX.endValue =
                close.springX.endValue + close.width / 2 - bubble.params.width / 2 + 2
            bubble.springY.endValue =
                close.springY.endValue + close.height / 2 - bubble.params.height / 2 + 2

            close.enlarge()

            postDelayed({
                hideAll()
                isClose=true
            }, 100)
        }
        if (wasMoving) {
            motionTrackerParams.x =
                if (isOnRight) metrics.widthPixels - bubble.width else 0

            lastY = bubble.springY.currentValue

            if (!detectedOutOfBounds && !closeCaptured && !closeVelocityCaptured) {
                if (bubble.springY.currentValue < 0) {
                    bubble.springY.endValue = 0.0
                    detectedOutOfBounds = true
                } else if (bubble.springY.currentValue > metrics.heightPixels) {
                    bubble.springY.endValue =
                        metrics.heightPixels - CHAT_HEAD_SIZE.toDouble()
                    detectedOutOfBounds = true
                }
            }

            if (!moving) {
                if (spring === bubble.springX) {
                    val xPosition = bubble.springX.currentValue
                    if (xPosition + bubble.width > metrics.widthPixels && bubble.springX.velocity > 0) {
                        val newPos =
                            metrics.widthPixels - bubble.width + CHAT_HEAD_OUT_OF_SCREEN_X
                        bubble.springX.springConfig = SpringConfigs.NOT_DRAGGING
                        bubble.springX.endValue = newPos.toDouble()
                        isOnRight = true
                    } else if (xPosition < 0 && bubble.springX.velocity < 0) {
                        bubble.springX.springConfig = SpringConfigs.NOT_DRAGGING
                        bubble.springX.endValue = -CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                        isOnRight = false
                    }
                } else if (spring === bubble.springY) {
                    val yPosition = bubble.springY.currentValue
                    if (yPosition + bubble.height > metrics.heightPixels && bubble.springY.velocity > 0) {
                        bubble.springY.springConfig = SpringConfigs.NOT_DRAGGING
                        bubble.springY.endValue =
                            metrics.heightPixels - bubble.height.toDouble() - dpToPx(
                                25f
                            )
                    } else if (yPosition < 0 && bubble.springY.velocity < 0) {
                        bubble.springY.springConfig = SpringConfigs.NOT_DRAGGING
                        bubble.springY.endValue = 0.0
                    }
                }
            }

            if (abs(totalVelocity) % 10 == 0 && !moving && bubble != null) {
                motionTrackerParams.y = bubble.springY.currentValue.toInt()

                BubbleService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)
            }
        }
    }

     fun onClose() {
        this.removeAllViews()
        BubbleService.instance.windowManager.removeView(this)
        BubbleService.instance.windowManager.removeView(motionTracker)
        closeCaptured = false
        movingOutOfClose = false
    }

    fun hideAll() {
        this.visibility = GONE
        motionTracker.visibility= GONE
    }

    fun showAll() {
        if (isClose){
            val metrics = getScreenSize()
            bubble.springX.endValue= (metrics.widthPixels-bubble.width).toDouble()
            bubble.springY.endValue= (metrics.heightPixels-bubble.height).toDouble()
        }
        this.visibility = VISIBLE
        motionTracker.visibility= VISIBLE
    }


}
