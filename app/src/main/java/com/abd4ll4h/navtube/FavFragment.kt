package com.abd4ll4h.navtube

import android.app.Dialog
import android.app.SearchManager
import android.app.SearchableInfo
import android.content.Context
import android.content.Context.SEARCH_SERVICE
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.setPadding
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.abd4ll4h.navtube.DataFetch.scraper.keyText
import com.abd4ll4h.navtube.adapters.FavListAdapter
import com.abd4ll4h.navtube.adapters.LabelListAdapter
import com.abd4ll4h.navtube.bubbleWidget.dpToPx
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import com.abd4ll4h.navtube.dataBase.tables.Label
import com.abd4ll4h.navtube.databinding.FragmentFavBinding
import com.abd4ll4h.navtube.utils.getDimensionFromAttribute
import com.abd4ll4h.navtube.viewModel.FavFragmentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class FavFragment : Fragment(), LabelListAdapter.ItemClick ,FavListAdapter.ItemClick{

    private var _binding: FragmentFavBinding? = null
    private val viewModel: FavFragmentViewModel by lazy {
        ViewModelProvider(this).get(FavFragmentViewModel::class.java)
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFavBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnMenuItemClicked()
        binding.addLabel.setOnClickListener { addLabelDialog(requireContext()) }
        val listAdapter = LabelListAdapter(this, listOf(),viewModel.checkedLabelList)
        binding.labelList.adapter = listAdapter
        binding.labelList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.labelList.setHasFixedSize(false)
        viewModel.labelsList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.noLabel.visibility = View.GONE

                listAdapter.submitList(it.reversed(),viewModel.checkedLabelList)
                binding.labelList.scrollToPosition(0)
            }else binding.noLabel.visibility=View.VISIBLE
        }
        val favListAdapter=FavListAdapter(this)
        binding.favList.adapter = favListAdapter
        binding.favList.layoutManager = LinearLayoutManager(requireContext())
        binding.favList.setHasFixedSize(false)
        binding.favList.addItemDecoration(MarginItemDecoration(16))
        viewModel.viewModelScope.launch {
            viewModel.favList.collect{
                favListAdapter.setItems(it)
            }
        }


    }

    private fun setOnMenuItemClicked() {
        val searchItem: MenuItem = binding.favToolBar.menu.findItem(R.id.fav_search)
        val searchView = searchItem.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setPadding(dpToPx(8f))

        searchView.queryHint=resources.getString(R.string.search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.searchFav(newText)
                return false
            }
        })
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

     private fun addLabelDialog(context: Context) {

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

    override fun onClick(label: Label, checked: Boolean) {
        viewModel.updateFavList()
    }

    override fun onPlayClicked(video: FavVideo) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(keyText.urlPrefix + video.id)))
    }

    override fun onFavClicked(videoTable: FavVideo, selected: Boolean) {
        if (selected) {
            videoTable.isFav = true
            viewModel.insertFAv(videoTable)
        }else viewModel.deleteFav(videoTable)
    }

    override fun onChannelClicked(video: FavVideo) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video.channelUrl)))
    }

    override fun onShareOptionClicked(video: FavVideo) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody = "Look what I found @NavTube " + keyText.urlPrefix + video.id
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    override fun onReportOptionClicked(video: Int) {
        Toast.makeText(context, "Soon", Toast.LENGTH_LONG).show()
    }

}