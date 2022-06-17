package com.abd4ll4h.navtube.bubbleWidget

import android.content.Context
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import com.abd4ll4h.navtube.R
import com.abd4ll4h.navtube.databinding.BubbleLayoutBinding
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringSystem

class Content(context: Context) : CardView(context) {
    private val springSystem = SpringSystem.create()
    private val scaleSpring = springSystem.createSpring()
    val binding: BubbleLayoutBinding
    var layoutManager = LinearLayoutManager(context)

    init {
        Log.i("check@!!!!!!!!!!", context.theme.toString())
        context.setTheme(R.style.Base_Theme_NavTube)
        binding = BubbleLayoutBinding.bind(inflate(context, R.layout.bubble_layout, this))

        scaleSpring.addListener(object : SimpleSpringListener() {
            override fun onSpringUpdate(spring: Spring) {
                scaleX = spring.currentValue.toFloat()
                scaleY = spring.currentValue.toFloat()
            }
        })
        scaleSpring.springConfig = SpringConfigs.CONTENT_SCALE

        scaleSpring.currentValue = 0.0

    }

    fun hideContent() {
        Log.i("check@bubble", "hide content")
        OverlayService.instance.chatHeads.showContentRunnable?.let {
            OverlayService.instance.chatHeads.handler.removeCallbacks(
                it
            )
        }

        scaleSpring.endValue = 0.0


        val anim = AlphaAnimation(1.0f, 0.0f)
        anim.duration = 200
        anim.repeatMode = Animation.RELATIVE_TO_SELF
        startAnimation(anim)
    }

    fun setInfo(activeChatHead: ChatHead) {
        Log.i("check@bubble", "hide content")
    }

    fun updateNavigationDrawer(activeChatHead: ChatHead) {
        Log.i("check@bubble", "nav drawer")
    }

    fun showContent() {
        Log.i("check@bubble", "show content")
        scaleSpring.endValue = 1.0

        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 100
        anim.repeatMode = Animation.RELATIVE_TO_SELF
        startAnimation(anim)
    }
}