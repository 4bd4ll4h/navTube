package com.abd4ll4h.navtube.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abd4ll4h.navtube.R
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import com.abd4ll4h.navtube.dataBase.tables.Label
import com.abd4ll4h.navtube.databinding.LoadingItemBinding
import com.abd4ll4h.navtube.databinding.MainCardBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringSystem
import com.facebook.rebound.SpringUtil
import jp.wasabeef.blurry.Blurry


class MainListAdapter(
    private val context: Context,
    private var videoList: ArrayList<FavVideo>,
    val itemClicked: ItemClick,
    val labels: LiveData<List<Label>>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val listItem = 1
    private val loadingItem = 0

    class LoadingViewHolder(private val binding: LoadingItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ViewHolder(
        val binding: MainCardBinding,
        val itemClicked: ItemClick,
        lifecycleOwner: LifecycleOwner
    ) :
        RecyclerView.ViewHolder(binding.root), LabelListAdapter.ItemClick {
        private val mScaleSpring: Spring = SpringSystem.create().createSpring();
        private var clickedView: View? = null
        val labelsAdapter:LabelListAdapter
        private fun setClickedView(it:View){
            clickedView=it
            mScaleSpring.endValue=1.0
            it.postDelayed({
                clickedView=it
                mScaleSpring.endValue=0.0},200)
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
                    itemClicked.onPlayClicked(videoList[adapterPosition])
                }
            }
            binding.addLabel.setOnClickListener { itemClicked.addLabelDialog()}
            binding.iconFav.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    videoList[adapterPosition].isFav = !videoList[adapterPosition].isFav
                    it.isSelected=videoList[adapterPosition].isFav
                   setClickedView(it)
                    itemClicked.onFavClicked(videoList[adapterPosition], it.isSelected)
                    if (videoList[adapterPosition].isFav){
                        binding.labelLayout.visibility=View.VISIBLE
                    }else {

                        binding.labelLayout.visibility=View.GONE
                    }
                }
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

            binding.iconCommunity.setOnClickListener{
                setClickedView(it)
            }
            binding.thumbnail.setOnClickListener {
                it.isSelected=!it.isSelected
                if (it.isSelected){
                    Blurry.with(it.context).capture(binding.thumbnail).into(it as ImageView)
                }
                else Glide.with(it.context)
                    .load(videoList[adapterPosition].thumbnailurl)
                    .centerCrop()
                    .into(it as ImageView)
            }
             labelsAdapter = LabelListAdapter(this, listOf())
            //nested recycleView fro label list
            binding.labelList.adapter = labelsAdapter
            binding.labelList.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.labelList.setHasFixedSize(false)
            labels.observe(lifecycleOwner){ list ->
                if (list.isNotEmpty()) {
                    binding.noLabel.visibility = View.GONE
                    labelsAdapter.submitList(
                        (list.reversed()),
                        arrayListOf()
                    )
                    binding.labelList.scrollToPosition(0)
                }else binding.noLabel.visibility=View.VISIBLE
            }
        }

        override fun onClick(label: Label, checked: Boolean) {
            itemClicked.onLabelClicked(label,videoList[adapterPosition])
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == listItem) {
            val binding =
                MainCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(binding, itemClicked, parent.findViewTreeLifecycleOwner()!!)
        } else {
            val view =
                LoadingItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )

            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            with(holder) {
                with(videoList[position]) {
                    binding.title.text = title
                    binding.channelName.text = creator

                    channelThumbnail?.let {
                        Glide.with(context)
                            .load(it)
                            .transform(CircleCrop())
                            .into(binding.channelThumbnail)
                        binding.channelThumbnail.visibility = View.VISIBLE
                    }

                    binding.thumbnail.isSelected=false
                    Glide.with(context)
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
                                binding.thumbnail.setImageDrawable(resource)
                                binding.relativeLayout.background =
                                    AppCompatResources.getDrawable(
                                        context,
                                        R.drawable.main_card_effect
                                    )


                                return true
                            }

                        })
                      //  .apply(requestOptions)

                        //.override(Target.SIZE_ORIGINAL)
                        .centerCrop()
                        .into(binding.thumbnail)
                    binding.iconFav.isSelected=videoList[position].isFav
                if (binding.iconFav.isSelected){
                    labelsAdapter.setCheckedChip(videoList[position].label)
                    binding.labelLayout.visibility=View.VISIBLE
                }else binding.labelLayout.visibility=View.GONE
                }

            }
        } else itemClicked.onListEnd()
    }

    fun setList(list: ArrayList<FavVideo>) {
        // this is for removing the loading list item in the end of the list
        if (videoList.isNotEmpty()) videoList.removeAt(videoList.lastIndex)
        videoList.addAll(list.filter {  return@filter !videoList.contains(it) })
        // this is for the loading list item in the end of the list
        if (videoList.isNotEmpty())
        videoList.add(videoList[0])
        notifyDataSetChanged()

    }
    fun clearAndSetList(list: ArrayList<FavVideo>){
        videoList=java.util.ArrayList(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1)
            loadingItem
        else listItem
    }

    interface ItemClick {
        fun onChannelClicked(video: FavVideo)
        fun onPlayClicked(video: FavVideo)
        fun onShareOptionClicked(video: FavVideo)
        fun onReportOptionClicked(video: Int)
        fun onListEnd()
        fun onFavClicked(FavVideo: FavVideo, selected: Boolean)
        fun onLabelClicked(label: Label, video: FavVideo)
        fun addLabelDialog()
    }
}