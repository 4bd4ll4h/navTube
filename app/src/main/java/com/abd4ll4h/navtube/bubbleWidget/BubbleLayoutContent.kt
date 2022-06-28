package com.abd4ll4h.navtube.bubbleWidget

import android.app.Application
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.view.Gravity
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope
import com.abd4ll4h.navtube.DataFetch.VideoTable
import com.abd4ll4h.navtube.DataFetch.scraper.keyText
import com.abd4ll4h.navtube.R
import com.abd4ll4h.navtube.databinding.BubbleLayoutBinding
import com.abd4ll4h.navtube.viewModel.MainFragmentViewModel
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringListener
import com.facebook.rebound.SpringSystem
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BubbleLayoutContent(val layout: BubbleLayout):ConstraintLayout(layout.context1),SpringListener {
    var springSystem: SpringSystem = SpringSystem.create()

    var springX: Spring = springSystem.createSpring()
    var springY: Spring = springSystem.createSpring()
    val mainFragmentViewModel=MainFragmentViewModel(layout.context1.applicationContext as Application)
    var params: WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        getOverlayFlag(),
        0,
        PixelFormat.TRANSLUCENT
    )
    lateinit var binding: BubbleLayoutBinding

    init {
        context.setTheme(R.style.Base_Theme_NavTube)
        this.clipToPadding=false
        binding= BubbleLayoutBinding.bind(inflate(context, R.layout.bubble_layout, this))
        params.gravity = Gravity.BOTTOM or Gravity.END

        layout.addView(this,params)

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
    }

    override fun onSpringUpdate(spring: Spring?) {
        if (spring !== this.springX && spring !== this.springY) return
        val totalVelocity = Math.hypot(springX.velocity, springY.velocity).toInt()

        layout.onChatHeadSpringUpdate( spring, totalVelocity)
    }

    override fun onSpringAtRest(spring: Spring?) {

    }

    override fun onSpringActivate(spring: Spring?) {

    }

    override fun onSpringEndStateChange(spring: Spring?) {

    }

    fun headOnClick() {
        GlobalScope.launch {
            onPlayClicked(mainFragmentViewModel.getVideoItem().value!!.random())
        }
    }
    fun onPlayClicked(video: VideoTable) {
        val intent=Intent(Intent.ACTION_VIEW, Uri.parse(keyText.urlPrefix + video.id))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(layout.context1,intent,null)
    }

}