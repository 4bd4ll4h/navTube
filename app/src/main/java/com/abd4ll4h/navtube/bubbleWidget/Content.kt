package com.abd4ll4h.navtube.bubbleWidget


import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.abd4ll4h.navtube.DataFetch.scraper.KeyText
import com.abd4ll4h.navtube.R
import com.abd4ll4h.navtube.adapters.LabelListAdapter
import com.abd4ll4h.navtube.bubbleWidget.BubbleService.Companion.bubbleScope
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import com.abd4ll4h.navtube.dataBase.tables.Label
import com.abd4ll4h.navtube.databinding.BubbleLayoutBinding
import com.abd4ll4h.navtube.viewModel.FavRepository
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringSystem
import com.facebook.rebound.SpringUtil
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch


class Content(val context1: Context) : FrameLayout(context1), LabelListAdapter.ItemClick {
    private val springSystem = SpringSystem.create()
    private val scaleSpring = springSystem.createSpring()
    private val mFavScaleSpring: Spring = SpringSystem.create().createSpring();
    lateinit var mVideo: FavVideo
    val binding: BubbleLayoutBinding
    val favRepository = FavRepository(context1)
    val labelListAdapter = LabelListAdapter(this, listOf())
    val checkedLabelList = ArrayList<Int>()
    var labelList = listOf<Label>()
    var currentVideo: FavVideo? = null

    init {
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

        binding.iconFav.setOnClickListener { it.isSelected = !it.isSelected; onFavClicked(it) }
        binding.share.setOnClickListener { onShareClicked() }

        mFavScaleSpring.addListener(object : SimpleSpringListener() {
            override fun onSpringUpdate(spring: Spring?) {
                super.onSpringUpdate(spring)
                val mappedValue = SpringUtil.mapValueFromRangeToRange(
                    spring!!.currentValue,
                    0.0,
                    1.0,
                    1.0,
                    0.5
                ).toFloat();
                binding.iconFav.scaleX = mappedValue;
                binding.iconFav.scaleY = mappedValue;
            }
        })
        binding.labelList.adapter = labelListAdapter
        binding.labelList.layoutManager =
            LinearLayoutManager(context1, LinearLayoutManager.HORIZONTAL, false)
        bubbleScope.launch {

            favRepository.getLabels().collect() {
                labelList = it
            }

        }
    }

    private fun onShareClicked() {
        if (currentVideo != null) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody =
            context.getString(R.string.look_what_I_found) + KeyText.urlPrefix + currentVideo!!.id
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(context1,
            Intent.createChooser(sharingIntent, context.getString(R.string.share_via))
                .addFlags(FLAG_ACTIVITY_NEW_TASK),
            null
        )
        }else {
            Toast.makeText(context1,"play a video from the bubble first to share it! ",Toast.LENGTH_SHORT).show()

        }
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
        binding.iconFav.isSelected = currentVideo?.isFav ?:false
        labelListAdapter.submitList(labelList, checkedLabelList)
        scaleSpring.springConfig = SpringConfigs.CONTENT_SCALE
        scaleSpring.endValue = 1.0
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 100
        anim.repeatMode = Animation.RELATIVE_TO_SELF
        startAnimation(anim)
    }

    internal fun onFavClicked(it: View?):Boolean {
        if (currentVideo != null) {
            currentVideo!!.isFav = it?.isSelected ?: !currentVideo!!.isFav
            bubbleScope.launch {
                favRepository.insertFav(currentVideo!!)
            }
            if (it != null) {
                mFavScaleSpring.endValue = 1.0
                it.postDelayed({

                    mFavScaleSpring.endValue = 0.0
                }, 200)
            }
            return true
        }else {
            Toast.makeText(context1,"play a video from the bubble first to favorite it! ",Toast.LENGTH_SHORT).show()
            return false
        }
    }

    fun updatelayout(video: FavVideo) {
        currentVideo = video
        checkedLabelList.clear()
        labelListAdapter.submitList(labelList, checkedLabelList)
        mVideo = video
        binding.iconFav.isSelected = currentVideo?.isFav ?:false
        binding.title.text = video.title
        Glide.with(context1)
            .load(video.thumbnailurl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {

                    binding.bubleThumbnail.setImageDrawable(resource)
                    Blurry.with(context1).capture(binding.bubleThumbnail)
                        .into(binding.bluryBackground)
                    return true
                }

            })
            .centerCrop()

            .into(binding.bubleThumbnail)
    }

    override fun onClick(label: Label, checked: Boolean) {
        if (checked) {
            checkedLabelList.clear()
            checkedLabelList.add(label.id)
        } else {
            checkedLabelList.remove(label.id)
        }
        if (currentVideo != null) {
            currentVideo!!.label = if (checked) label.id else null
            bubbleScope.launch {
                favRepository.updateFav(currentVideo!!)
            }
        }

    }
}

