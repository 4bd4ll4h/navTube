package com.abd4ll4h.navtube.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.abd4ll4h.navtube.R
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import com.abd4ll4h.navtube.databinding.FavCardItemBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringSystem
import com.facebook.rebound.SpringUtil
import jp.wasabeef.blurry.Blurry

class FavListAdapter(val itemClicked: ItemClick): ListAdapter<FavVideo, FavListAdapter.ViewHolder>(config) {
    object config : DiffUtil.ItemCallback<FavVideo>() {
        override fun areItemsTheSame(oldItem: FavVideo, newItem: FavVideo): Boolean = oldItem.id==newItem.id

        override fun areContentsTheSame(oldItem: FavVideo, newItem: FavVideo): Boolean = oldItem==newItem

    }

    inner class ViewHolder(val binding: FavCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val mScaleSpring: Spring = SpringSystem.create().createSpring();
        private var clickedView: View? = null

        private fun setClickedView(it: View) {
            clickedView = it
            mScaleSpring.endValue = 1.0
            it.postDelayed({
                clickedView = it
                mScaleSpring.endValue = 0.0
            }, 200)
        }

        init {
            mScaleSpring.addListener(object : SimpleSpringListener() {
                override fun onSpringUpdate(spring: Spring?) {
                    super.onSpringUpdate(spring)
                    val mappedValue = SpringUtil.mapValueFromRangeToRange(
                        spring!!.currentValue,
                        0.0,
                        1.0,
                        1.0,
                        0.5
                    ).toFloat();
                    clickedView!!.scaleX = mappedValue;
                    clickedView!!.scaleY = mappedValue;
                }
            })
            binding.iconPlay.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    setClickedView(it)
                    itemClicked.onPlayClicked(getItem(adapterPosition))
                }
            }
            binding.iconFav.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    getItem(adapterPosition).isFav = !getItem(adapterPosition).isFav
                    it.isSelected = getItem(adapterPosition).isFav
                    setClickedView(it)
                    itemClicked.onFavClicked(getItem(adapterPosition), it.isSelected)
                }
            }
            binding.channelLayout.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    itemClicked.onChannelClicked(getItem(adapterPosition))
            }
            binding.iconMore.setOnClickListener {

                if (adapterPosition != RecyclerView.NO_POSITION) {

                    val popup = PopupMenu(binding.root.context, it)
                    popup.inflate(R.menu.fav_card_option_menu)

                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.fav_share_option -> {
                                itemClicked.onShareOptionClicked(getItem(adapterPosition))
                            }
                            R.id.fav_move -> {
                                itemClicked.onMoveItemClicked(getItem(adapterPosition))
                            }

                        }
                        false
                    }
                    popup.show()
                }
            }
            binding.thumbnail.setOnClickListener {
                it.isSelected=!it.isSelected
                if (it.isSelected){
                    Blurry.with(it.context).capture(binding.thumbnail).into(it as ImageView)
                }
                else Glide.with(it.context)
                    .load(getItem(adapterPosition).thumbnailurl)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .centerCrop()
                    .into(it as ImageView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(FavCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(getItem(position)) {
                binding.title.text = title
                binding.channelName.text = creator
                var requestOptions = RequestOptions()
                requestOptions = requestOptions.fitCenter()
                channelThumbnail?.let {
                    Glide.with(itemView.context.applicationContext)
                        .load(it)
                        .transform(CircleCrop())
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(binding.channelThumbnail)
                    binding.channelThumbnail.visibility = View.VISIBLE
                }
                Glide.with(itemView.context.applicationContext)
                    .load(thumbnailurl)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
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
                            binding.thumbnail.setImageDrawable(resource)
                            binding.relativeLayout.background =
                                AppCompatResources.getDrawable(
                                    itemView.context.applicationContext,
                                    R.drawable.fav_card_effect
                                )
                            return true
                        }

                    })
                    //.apply(requestOptions)
                    .centerCrop()
                    .into(binding.thumbnail)


                binding.iconFav.isSelected=isFav
            }
        }
    }

    fun setItems(list: List<FavVideo>) {
        submitList(List(list.size){
            index -> return@List list[index]
        })
    }

    interface ItemClick {
        fun onPlayClicked(video: FavVideo)
        fun onFavClicked(videoTable: FavVideo, selected: Boolean)
        fun onChannelClicked(video: FavVideo)
        fun onShareOptionClicked(video: FavVideo)
        fun onMoveItemClicked(video: FavVideo)
    }
}