package com.abd4ll4h.navtube.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.abd4ll4h.navtube.DataFetch.VideoTable
import com.abd4ll4h.navtube.R
import com.abd4ll4h.navtube.databinding.MainCardBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target


class MainListAdapter(private val context: Context, private val videoList:ArrayList<VideoTable>,val itemClicked:ItemClick): RecyclerView.Adapter<MainListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: MainCardBinding, val itemClicked: ItemClick) : RecyclerView.ViewHolder(binding.root){
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
                    val popup = PopupMenu(context, it)

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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MainCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding,itemClicked)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            with(videoList[position]){
                binding.title.text=title
                binding.channelName.text=creator
                var requestOptions = RequestOptions()
                requestOptions = requestOptions.fitCenter()
                channelThumbnail?.let {
                    Glide.with(itemView.context)
                        .load(it)
                        .apply(requestOptions)
                        .into(binding.channelThumbnail)
                    binding.channelThumbnail.visibility=View.VISIBLE
                }

                Glide.with(itemView.context)
                    .load(thumbnailurl)
                    .listener(object : RequestListener<Drawable>{
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
                                AppCompatResources.getDrawable(context,R.drawable.main_card_effect)
                            return false
                        }

                    })
                    .apply(requestOptions)
                    .override(Target.SIZE_ORIGINAL)
                    .into(binding.thumbnail)
            }

        }
    }
    fun setList(list: ArrayList<VideoTable>) {
        videoList.addAll(list)
        notifyItemInserted(itemCount)
    }
    override fun getItemCount(): Int {
        return videoList.size
    }
interface ItemClick{
    fun onChannelClicked(video: VideoTable)
    fun onPlayClicked(video: VideoTable)
    fun onShareOptionClicked(video: VideoTable)
    fun onReportOptionClicked(video: Int)
}
}