package com.abd4ll4h.navtube.bubbleWidget

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.abd4ll4h.navtube.R
import com.facebook.rebound.*
import com.facebook.rebound.SpringSystem
import kotlin.math.hypot
import kotlin.math.pow


class ChatHead(var chatHeads: ChatHeads): FrameLayout(chatHeads.context), View.OnTouchListener, SpringListener {
    var params: WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        getOverlayFlag(),
        0,
        PixelFormat.TRANSLUCENT
    )

    var springSystem: SpringSystem = SpringSystem.create()

    var springX: Spring = springSystem.createSpring()
    var springY: Spring = springSystem.createSpring()

    private val paint = Paint()

    private var initialX = 0.0f
    private var initialY = 0.0f

    private var initialTouchX = 0.0f
    private var initialTouchY = 0.0f

    private var moving = false



    override fun onSpringEndStateChange(spring: Spring?) = Unit
    override fun onSpringAtRest(spring: Spring?) = Unit
    override fun onSpringActivate(spring: Spring?) = Unit

    init {
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 0
        params.width = ChatHeads.CHAT_HEAD_SIZE + 15
        params.height = ChatHeads.CHAT_HEAD_SIZE + 30

        val view = inflate(context, R.layout.bubble, this)


        springX.addListener(object : SimpleSpringListener() {
            override fun onSpringUpdate(spring: Spring) {
                x = spring.currentValue.toFloat()
            }
        })

        springX.springConfig = SpringConfigs.NOT_DRAGGING
        springX.addListener(this)

        springY.addListener(object : SimpleSpringListener() {
            override fun onSpringUpdate(spring: Spring) {
                y = spring.currentValue.toFloat()
            }
        })
        springY.springConfig = SpringConfigs.NOT_DRAGGING
        springY.addListener(this)

        this.setLayerType(View.LAYER_TYPE_HARDWARE, paint)

        chatHeads.addView(this, params)

        this.setOnTouchListener(this)


        view.findViewById<ImageButton>(R.id.close_button).setImageResource(R.drawable.ic_baseline_close_24)
        view.findViewById<ImageButton>(R.id.close_button).setImageResource(R.drawable.ic_outline_settings_24)

    }


    override fun onSpringUpdate(spring: Spring) {
        if (spring !== this.springX && spring !== this.springY) return
        val totalVelocity = hypot(springX.velocity, springY.velocity).toInt()

        chatHeads.onChatHeadSpringUpdate(this, spring, totalVelocity)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val currentChatHead = chatHeads.chatHeads.find { it == v }!!

        val metrics = getScreenSize()
        Log.i("check@bubble","touch head")
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = x
                initialY = y
                initialTouchX = event.rawX
                initialTouchY = event.rawY

                scaleX = 0.92f
                scaleY = 0.92f
            }
            MotionEvent.ACTION_UP -> {
                Log.i("check@bubble","touch up button")

                if (!moving) {
                    if (currentChatHead == chatHeads.activeChatHead) {
                        chatHeads.collapse()
                    } else {
                        chatHeads.activeChatHead = currentChatHead
                        chatHeads.updateActiveContent()
                    }
                } else {
                    springX.endValue = metrics.widthPixels - width - 1 * (width + ChatHeads.CHAT_HEAD_EXPANDED_PADDING).toDouble()
                    springY.endValue =  metrics.heightPixels - height.toDouble()- ChatHeads.CHAT_HEAD_EXPANDED_MARGIN_TOP

                    if (this == chatHeads.activeChatHead) {
                        chatHeads.content.showContent()
                    }
                }

                scaleX = 1f
                scaleY = 1f

                moving = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (ChatHeads.distance(initialTouchX, event.rawX, initialTouchY, event.rawY) > ChatHeads.CHAT_HEAD_DRAG_TOLERANCE.pow(2) && !moving) {
                    moving = true

                    if (this == chatHeads.activeChatHead) {
                        chatHeads.content.hideContent()
                    }
                }

                if (moving) {
                    springX.currentValue = initialX + (event.rawX - initialTouchX).toDouble()
                    springY.currentValue = initialY + (event.rawY - initialTouchY).toDouble()
                }
            }
        }

        return true
    }
}