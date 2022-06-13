package com.abd4ll4h.navtube.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.abd4ll4h.navtube.DataFetch.VideoTable
import com.abd4ll4h.navtube.R
import com.abd4ll4h.navtube.databinding.LoadingItemBinding
import com.abd4ll4h.navtube.databinding.MainCardBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target


class MainListAdapter(
    private val context: Context,
    private var videoList: ArrayList<VideoTable>,
    val itemClicked: ItemClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val listItem = 1
    private val loadingItem = 0

    class LoadingViewHolder(private val binding: LoadingItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ViewHolder(val binding: MainCardBinding, val itemClicked: ItemClick) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.iconPlay.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    itemClicked.onPlayClicked(videoList[adapterPosition])
            }

            binding.channelLayout.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    itemClicked.onChannelClicked(videoList[adapterPosition])
            }
            binding.iconMore.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val popup = PopupMenu(binding.root.context, it)

                    popup.inflate(R.menu.main_card_option_menu)

                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.share_option -> {
                                itemClicked.onShareOptionClicked(videoList[adapterPosition])
                            }
                            R.id.report_option -> {
                                itemClicked.onReportOptionClicked(adapterPosition)
                            }

                        }
                        false
                    }
                    popup.show()
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == listItem) {
            val binding =
                MainCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding, itemClicked)
        } else {
            val view =
                LoadingItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )

            return LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            with(holder) {
                with(videoList[position]) {
                    binding.title.text = title
                    binding.channelName.text = creator
                    var requestOptions = RequestOptions()
                    requestOptions = requestOptions.fitCenter()
                    channelThumbnail?.let {
                        Glide.with(itemView.context)
                            .load(it)
                            .apply(requestOptions)
                            .into(binding.channelThumbnail)
                        binding.channelThumbnail.visibility = View.VISIBLE
                    }

                    Glide.with(itemView.context)
                        .load(thumbnailurl)
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
                                binding.relativeLayout.background =
                                    AppCompatResources.getDrawable(
                                        context,
                                        R.drawable.main_card_effect
                                    )
                                return false
                            }

                        })
                        .apply(requestOptions)
                        .override(Target.SIZE_ORIGINAL)
                        .into(binding.thumbnail)
                }

            }
        }
        else itemClicked.onListEnd()
    }

    fun setList(list: ArrayList<VideoTable>) {
        videoList=ArrayList(list)
        notifyDataSetChanged()

    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount-1)
            loadingItem
        else listItem
    }

    interface ItemClick {
        fun onChannelClicked(video: VideoTable)
        fun onPlayClicked(video: VideoTable)
        fun onShareOptionClicked(video: VideoTable)
        fun onReportOptionClicked(video: Int)
        fun onListEnd()
    }
}