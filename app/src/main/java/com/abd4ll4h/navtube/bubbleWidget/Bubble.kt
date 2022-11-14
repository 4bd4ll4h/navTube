package com.abd4ll4h.navtube.bubbleWidget

import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.res.ResourcesCompat
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.abd4ll4h.navtube.DataFetch.ResponseWrapper
import com.abd4ll4h.navtube.DataFetch.scraper.KeyText
import com.abd4ll4h.navtube.R
import com.abd4ll4h.navtube.bubbleWidget.BubbleService.Companion.bubbleScope
import com.abd4ll4h.navtube.bubbleWidget.BubbleService.Companion.repository
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import com.abd4ll4h.navtube.databinding.BubbleBinding
import com.abd4ll4h.navtube.utils.ConnectionLiveData
import com.facebook.rebound.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import java.lang.Runnable

class Bubble(val layout: BubbleLayout) : FrameLayout(layout.context1),
    SpringListener {
    var springSystem: SpringSystem = SpringSystem.create()
    val rotateAnimation: SpringAnimation
    var springX: Spring = springSystem.createSpring()
    var springY: Spring = springSystem.createSpring()
    val mScaleSpring: Spring = SpringSystem.create().createSpring();
    var stateChangeHandler: Runnable? = null
    var lastIndex: Int = -1
    var hasErorr = false
    var isclickable = true
    var isConnection=false
    var params: WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        getOverlayFlag(),
        0,
        PixelFormat.TRANSLUCENT
    )

    private val networkObserver = ConnectionLiveData(layout.context1)
    private val bubbleScope = CoroutineScope(Dispatchers.IO)
    private val videoList = repository.videoFlow.stateIn(
        bubbleScope, SharingStarted.WhileSubscribed(5000),
        ResponseWrapper.loading(emptyList())
    )
    lateinit var binding: BubbleBinding

    init {
        context.setTheme(R.style.Base_Theme_NavTube)
        this.clipToPadding = false
        this.clipChildren = false
        binding = BubbleBinding.bind(inflate(context, R.layout.bubble, this))
        rotateAnimation = createSpringAnimation(
            binding.bubbleAvatar,
            SpringAnimation.ROTATION,
            0f,
            SpringForce.STIFFNESS_VERY_LOW,
            0.80f
        )

        params.gravity = Gravity.BOTTOM or Gravity.END

        layout.addView(this, params)


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

        mScaleSpring.addListener(object : SimpleSpringListener() {
            override fun onSpringUpdate(spring: Spring?) {
                super.onSpringUpdate(spring)
                val mappedValue = SpringUtil.mapValueFromRangeToRange(
                    spring!!.currentValue,
                    0.0,
                    1.0,
                    1.0,
                    1.2
                ).toFloat();
                binding.bubbleAvatar.scaleX = mappedValue;
                binding.bubbleAvatar.scaleY = mappedValue;
            }
        })

       bubbleScope.launch {

            videoList.collectLatest {
                Log.i("check_tag", "bubble: " + it.status.name + it.data.size)
                when (it.status) {
                    ResponseWrapper.Status.ERROR -> {
                        withContext(Dispatchers.Main){
                            Toast.makeText(
                            context,
                            "Error while loading click on the bubble again to reload",
                            Toast.LENGTH_LONG
                        ).show()
                        }
                        hasErorr = true
                    }
                    ResponseWrapper.Status.SUCCESS -> {
                        isclickable = true
                        hasErorr = false
                        lastIndex = -1
                        Log.i("checka","here:"+ it.data.size)
                    }
                    ResponseWrapper.Status.LOADING -> {
                      //  isclickable = false


                    }
                }
            }
        }


        networkObserver.observeForever() {
            isConnection=it
            if (it) {
                if (layout.isClose) {
                    Log.i("checka","here0")

                    isclickable=false
                    bubbleScope.launch {
                        repository.loadVidData(KeyText.genrateID())
                    }
                    layout.showAll()
                    layout.isClose = false
                } else {
                    stateChangeHandler?.let { it1 -> handler.removeCallbacks(it1) }
                    layout.showAll()
                    binding.bubbleAvatar.isClickable = true
                    binding.bubbleAvatar.setBackgroundColor(Color.GREEN)
                    binding.bubbleAvatar.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_baseline_cloud_24,
                            context.theme
                        )
                    )
                    mScaleSpring.endValue = 1.2
                    postDelayed({
                        mScaleSpring.endValue = 0.0
                        binding.bubbleAvatar.setBackgroundColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.primaryDark,
                                null
                            )
                        )
                        binding.bubbleAvatar.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.ic_small_logo,
                                context.theme
                            )
                        )
                    }, 1500)
                }
            } else {
                if (!layout.isClose) {
                    binding.bubbleAvatar.setBackgroundColor(Color.RED)
                    binding.bubbleAvatar.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_baseline_cloud_off_24,
                            context.theme
                        )
                    )
                    binding.bubbleAvatar.isClickable = false
                    mScaleSpring.endValue = 1.2
                    postDelayed({ mScaleSpring.endValue = 0.0 }, 700)
                    stateChangeHandler?.let { it1 -> handler.removeCallbacks(it1) }
                    stateChangeHandler = Runnable {
                        layout.hideAll()
                    }
                    handler.postDelayed(stateChangeHandler!!, 3000)
                }
            }
        }
    }

    private fun createSpringAnimation(
        view: View,
        property: DynamicAnimation.ViewProperty,
        finalPosition: Float,
        stiffness: Float,
        dampingRatio: Float
    ): SpringAnimation {
        val animation = SpringAnimation(view, property)
        val spring = SpringForce(finalPosition)
        spring.stiffness = stiffness
        spring.dampingRatio = dampingRatio

        animation.spring = spring
        return animation
    }

    override fun onSpringUpdate(spring: Spring?) {
        if (spring !== this.springX && spring !== this.springY) return
        val totalVelocity = Math.hypot(springX.velocity, springY.velocity).toInt()

        layout.onChatHeadSpringUpdate(spring, totalVelocity)
    }

    override fun onSpringAtRest(spring: Spring?) {

    }

    override fun onSpringActivate(spring: Spring?) {

    }

    override fun onSpringEndStateChange(spring: Spring?) {

    }

    fun headOnClick() {
        Log.i("adfafdf","size= "+videoList.value.data.size)
        if (isclickable) {
            if ((lastIndex + 1) >= videoList.value.data.size) {
                Toast.makeText(
                    context,
                    resources.getText(R.string.loadingVideo),
                    Toast.LENGTH_SHORT
                ).show()
                playAnimation()
                isclickable = false
                bubbleScope.launch {
                    repository.loadVidData(KeyText.genrateID())
                }
            } else {
                val currentVideo = videoList.value.data[++lastIndex]
                layout.content.updatelayout(currentVideo)
                onPlayClicked(currentVideo)
            }
            playAnimation()
        } else {
            Toast.makeText(context,  resources.getText(R.string.loadingVideo), Toast.LENGTH_SHORT).show()
            if (hasErorr) bubbleScope.launch {
                hasErorr=false
                repository.loadVidData(KeyText.genrateID())
            }
        }
    }

    private fun playAnimation() {
        rotateAnimation.cancel()
        with(binding.bubbleAvatar) {
            rotation = -85f
            rotateAnimation.start()
            handler.postDelayed({
                rotateAnimation.cancel()
                rotation = 360f
                rotateAnimation.start()
            }, 400)

        }
    }

    fun onPlayClicked(video: FavVideo) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(KeyText.urlPrefix + video.id))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(layout.context1, intent, null)
    }

    fun showContent() {
        binding.bubbleAvatar.visibility = GONE
        binding.cardLong.visibility = VISIBLE

    }


    fun hideContent() {
        binding.bubbleAvatar.visibility = VISIBLE
        binding.cardLong.visibility = GONE

    }


}