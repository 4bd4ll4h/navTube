package com.abd4ll4h.navtube

import android.app.Dialog
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.abd4ll4h.navtube.DataFetch.VideoTable
import com.abd4ll4h.navtube.DataFetch.scraper.keyText.urlPrefix
import com.abd4ll4h.navtube.adapters.MainListAdapter
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import com.abd4ll4h.navtube.dataBase.tables.Label
import com.abd4ll4h.navtube.databinding.FragmentMainBinding
import com.abd4ll4h.navtube.utils.getDimensionFromAttribute
import com.abd4ll4h.navtube.viewModel.FavFragmentViewModel
import com.abd4ll4h.navtube.viewModel.MainFragmentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class MainFragment : Fragment(), MainListAdapter.ItemClick {

    private var _binding: FragmentMainBinding? = null
    val binding get() = _binding!!
    private val viewModel: MainFragmentViewModel by lazy {
        ViewModelProvider(this).get(MainFragmentViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setOnMenuItemClicked()
        super.onViewCreated(view, savedInstanceState)

        val listAdapter =
            MainListAdapter(requireContext().applicationContext, ArrayList<VideoTable>(), this,viewModel.getLabels())

        binding.mainList.adapter = listAdapter
        binding.mainList.layoutManager = LinearLayoutManager(requireContext())
        binding.mainList.setHasFixedSize(false)
        binding.mainList.addItemDecoration(MarginItemDecoration(8))
        lifecycleScope.launchWhenResumed {
            viewModel.getVideoItem().observe(viewLifecycleOwner) {
                binding.swipeRefreshLayout.isRefreshing = false
                Log.i("_check",it.size.toString())
                listAdapter.setList(it)

            }
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                viewModel.refreshList()
            }
        }


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
            viewLifecycleOwner.lifecycleScope.launch {
                onPlayClicked(viewModel.getVideoItem().value!!.random())
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null

    }

    override fun onChannelClicked(video: VideoTable) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video.channelUrl)))
    }

    override fun onPlayClicked(video: VideoTable) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlPrefix + video.id)))
    }

    override fun onShareOptionClicked(video: VideoTable) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody = "Look what I found @NavTube " + urlPrefix + video.id
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    override fun onReportOptionClicked(video: Int) {
        Toast.makeText(context, "Soon", Toast.LENGTH_LONG).show()
    }

    override fun onListEnd() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadNewVideo()
        }
    }

    override fun onFavClicked(videoTable: FavVideo, selected: Boolean) {
        if (selected) {
            videoTable.isFav = true
            viewModel.insertFAv(videoTable)
        }else viewModel.deleteFav(videoTable)
    }

    override fun onLabelClicked(label: Label, video: FavVideo) {
        if (!video.isFav)video.isFav=true
        video.label=label.id
        viewModel.updateFav(video)
    }

    override fun addLabelDialog() {
        val context= requireContext()
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




}