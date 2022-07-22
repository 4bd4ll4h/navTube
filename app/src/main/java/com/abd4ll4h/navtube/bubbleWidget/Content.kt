package com.abd4ll4h.navtube.bubbleWidget



import android.content.Context
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.abd4ll4h.navtube.R
import com.abd4ll4h.navtube.databinding.BubbleBinding
import com.abd4ll4h.navtube.databinding.BubbleLayoutBinding
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringSystem


class Content(context: Context): ConstraintLayout(context) {
    private val springSystem = SpringSystem.create()
    private val scaleSpring = springSystem.createSpring()
    val binding:BubbleLayoutBinding

    init {
        context.setTheme(R.style.Base_Theme_NavTube)
        binding= BubbleLayoutBinding.bind(inflate(context, R.layout.bubble_layout, this))

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
//        OverlayService.instance.chatHeads.handler.removeCallbacks(
//            OverlayService.instance.chatHeads.showContentRunnable)
        scaleSpring.springConfig = SpringConfigs.CLOSE_SCALE
        scaleSpring.endValue = 0.0



        val anim = AlphaAnimation(1.0f, 0.0f)
        anim.duration = 200
        anim.repeatMode = Animation.RELATIVE_TO_SELF
        startAnimation(anim)
    }

    fun showContent() {
        scaleSpring.springConfig = SpringConfigs.CONTENT_SCALE
        scaleSpring.endValue = 1.0
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 100
        anim.repeatMode = Animation.RELATIVE_TO_SELF
        startAnimation(anim)
    }
}

