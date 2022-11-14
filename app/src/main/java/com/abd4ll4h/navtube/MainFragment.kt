package com.abd4ll4h.navtube

import android.app.Dialog
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.abd4ll4h.navtube.DataFetch.ResponseWrapper.Status.*
import com.abd4ll4h.navtube.DataFetch.scraper.KeyText
import com.abd4ll4h.navtube.DataFetch.scraper.KeyText.urlPrefix
import com.abd4ll4h.navtube.adapters.MainListAdapter
import com.abd4ll4h.navtube.bubbleWidget.BubbleService
import com.abd4ll4h.navtube.bubbleWidget.dpToPx
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import com.abd4ll4h.navtube.dataBase.tables.Label
import com.abd4ll4h.navtube.databinding.FragmentMainBinding
import com.abd4ll4h.navtube.utils.ConnectionLiveData
import com.abd4ll4h.navtube.utils.MarginItemDecoration
import com.abd4ll4h.navtube.utils.getDimensionFromAttribute
import com.abd4ll4h.navtube.viewModel.MainFragmentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch


class MainFragment : Fragment(), MainListAdapter.ItemClick {

    private var _binding: FragmentMainBinding? = null
    val binding get() = _binding!!
    var isRefresh = false


    private val viewModel: MainFragmentViewModel by lazy {
        ViewModelProvider(this).get(MainFragmentViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setOnMenuItemClicked()
        super.onViewCreated(view, savedInstanceState)

        val listAdapter = MainListAdapter(
            requireContext().applicationContext,
            ArrayList<FavVideo>(),
            this,
            viewModel.getLabels()
        )

        binding.mainList.adapter = listAdapter
        binding.mainList.layoutManager = LinearLayoutManager(requireContext())
        binding.mainList.setHasFixedSize(false)
        binding.mainList.addItemDecoration(MarginItemDecoration(dpToPx(15f)))
        binding.swipeRefreshLayout.isRefreshing = true

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.videoItem.collect {
                    when (it.status) {
                        SUCCESS -> {
                            binding.swipeRefreshLayout.isRefreshing = false
                            if (isRefresh) {
                                isRefresh=false
                                listAdapter.clearAndSetList(it.data as ArrayList<FavVideo>)
                            }
                            else listAdapter.setList(it.data as ArrayList<FavVideo>)
                            if (it.data.isNotEmpty()) {
                                binding.fab.alpha = 1f
                                binding.noListLayout.visibility = View.GONE
                                binding.mainList.visibility = View.VISIBLE

                            } else binding.noListLayout.visibility = View.VISIBLE

                        }
                        ERROR -> {
                            binding.swipeRefreshLayout.isRefreshing = false
                            showMessage(it.message)
                            if (it.data.isNotEmpty()) binding.fab.alpha = 1f
                            else {
                                binding.noListLayout.visibility = View.VISIBLE
                                binding.mainList.visibility = GONE
                            }
                        }
                        LOADING -> {
                            if (it.data.isEmpty()) binding.fab.alpha = 0.5f
                            binding.noListLayout.visibility = View.GONE
                        }
                    }
                }
            }

        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            isRefresh = true
            viewModel.loadNewVideo()
        }


    }

    private fun showMessage(message: String?) {
        Toast.makeText(this.context, message, Toast.LENGTH_LONG).show()
    }

    fun smoothScrollToTop() {
        val smoothScroller: SmoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        smoothScroller.targetPosition = 0
        binding.mainList.layoutManager!!.startSmoothScroll(smoothScroller);
    }

    private fun setOnMenuItemClicked() {
        binding.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_settings -> {
                    Toast.makeText(context, "Soon", Toast.LENGTH_LONG).show()
                    true
                }
                R.id.mainFilter -> {
                    Toast.makeText(context, "Soon", Toast.LENGTH_LONG).show()
                    true
                }
                else -> false
            }

        }
        binding.toolBar.menu.getItem(0).actionView.setOnClickListener {
            Toast.makeText(context, "Soon", Toast.LENGTH_LONG).show()
        }

    }

    override fun onStart() {
        super.onStart()
        binding.fab.setOnClickListener {
            if (viewModel.videoItem.value.data.isNotEmpty())
                onPlayClicked(viewModel.videoItem.value.data[0])
            else showMessage(resources.getString(R.string.NoVideoPleseResfresh))

        }
    }


    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null

    }

    override fun onChannelClicked(video: FavVideo) {
        startService()
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video.channelUrl)))
    }

    override fun onPlayClicked(video: FavVideo) {
        startService()
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlPrefix + video.id)))
    }

    override fun onShareOptionClicked(video: FavVideo) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody =
            resources.getString(R.string.look_what_I_found) + KeyText.urlPrefix + video.id
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, resources.getString(R.string.share_via)))
    }

    override fun onReportOptionClicked(video: Int) =
        Toast.makeText(context, "Soon", Toast.LENGTH_LONG).show()


    override fun onListEnd() = viewModel.loadNewVideo()


    override fun onFavClicked(video: FavVideo, selected: Boolean) {
        if (selected) {
            video.isFav = true
            viewModel.insertFAv(video)
        } else viewModel.deleteFav(video)
    }

    override fun onLabelClicked(label: Label, video: FavVideo) {
        if (!video.isFav) video.isFav = true
        video.label = label.id
        viewModel.updateFav(video)
    }

    override fun addLabelDialog() {
        val context = requireContext()
        val con = ContextWrapper(context)

        con.setTheme(R.style.PopupDialog)

        val textInputLayout = TextInputLayout(con)
        textInputLayout.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        textInputLayout.isCounterEnabled = true
        textInputLayout.setPadding(
            getDimensionFromAttribute(context, android.R.attr.dialogPreferredPadding),
            getDimensionFromAttribute(context, android.R.attr.dialogPreferredPadding), 8, 0
        )
        textInputLayout.setBoxCornerRadii(10f, 10f, 10f, 10f)
        textInputLayout.counterMaxLength = 15
        val editText = TextInputEditText(textInputLayout.context)
        editText.setHint(R.string.label_name)
        textInputLayout.addView(editText)
        val dialog = MaterialAlertDialogBuilder(context, R.style.PopupDialog)
            .setView(textInputLayout)
            .setPositiveButton(R.string.save) { dialog, which ->

                viewModel.addLabel(editText.text!!.trim().toString())
                Toast.makeText(context, editText.text, Toast.LENGTH_LONG).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.show()
        dialog.getButton(Dialog.BUTTON_POSITIVE).isClickable = false
        dialog.getButton(Dialog.BUTTON_POSITIVE).alpha = 0.5f
        context.setTheme(R.style.Base_Theme_NavTube)

        editText.addTextChangedListener { text ->
            if (text!!.isNotEmpty() && text.length <= 15) {
                dialog.getButton(Dialog.BUTTON_POSITIVE).isClickable = true
                dialog.getButton(Dialog.BUTTON_POSITIVE).alpha = 1f
            } else {
                dialog.getButton(Dialog.BUTTON_POSITIVE).isClickable = false
                dialog.getButton(Dialog.BUTTON_POSITIVE).alpha = 0.5f

            }


        }
    }

    private fun startService() {
        binding.root.postDelayed({
            val service = Intent(context, BubbleService::class.java)
            ContextCompat.startForegroundService(requireContext(), service)
        }, 2000)

    }

    fun connectivityChanged(isConnected: Boolean) {
        if (isConnected) {

            binding.fab.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_baseline_cloud_24,
                    requireContext().theme
                )
            )
            binding.fab.imageTintList = ColorStateList.valueOf(Color.GREEN)
            binding.fab.postDelayed({
                binding.fab.imageTintList = null
                binding.fab.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.nav_logo,
                        requireContext().theme
                    )
                )

            }, 1500)

        } else {

            binding.fab.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_baseline_cloud_off_24,
                    requireContext().theme
                )
            )
            binding.fab.imageTintList = ColorStateList.valueOf(Color.RED)
            showMessage(resources.getString(R.string.no_Internet))
        }
    }
}