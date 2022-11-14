package com.abd4ll4h.navtube

import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.abd4ll4h.navtube.DataFetch.scraper.KeyText
import com.abd4ll4h.navtube.adapters.FavListAdapter
import com.abd4ll4h.navtube.adapters.LabelListAdapter
import com.abd4ll4h.navtube.bubbleWidget.BubbleService
import com.abd4ll4h.navtube.bubbleWidget.dpToPx
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import com.abd4ll4h.navtube.dataBase.tables.Label
import com.abd4ll4h.navtube.databinding.FragmentFavBinding
import com.abd4ll4h.navtube.utils.MarginItemDecoration
import com.abd4ll4h.navtube.utils.getDimensionFromAttribute
import com.abd4ll4h.navtube.viewModel.FavFragmentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
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
        setToolBar()

        binding.addLabel.setOnClickListener { addLabelDialog(requireContext()) }
        val listAdapter = LabelListAdapter(this, listOf(),viewModel.checkedLabelList.value)
        binding.labelList.adapter = listAdapter
        binding.labelList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.labelList.setHasFixedSize(false)
        lifecycleScope.launch {
            viewModel.labelsList.collect{

                if (it.isNotEmpty()) {
                    setToolBar()
                    binding.noLabel.visibility = View.GONE
                    listAdapter.submitList(it.reversed(),viewModel.checkedLabelList.value)
                    binding.labelList.visibility=View.VISIBLE
                    binding.labelList.scrollToPosition(0)
                }else {
                    binding.noLabel.visibility=View.VISIBLE
                    listAdapter.submitList(it,viewModel.checkedLabelList.value)
                    binding.labelList.visibility=View.GONE
                }
            }
        }

        val favListAdapter=FavListAdapter(this)
        binding.favList.adapter = favListAdapter
        binding.favList.layoutManager = LinearLayoutManager(requireContext())
        binding.favList.setHasFixedSize(false)
        binding.favList.addItemDecoration(MarginItemDecoration(16))
        lifecycleScope.launch {
            viewModel.favList.collect{
                if (it.isEmpty()){
                    binding.noListLayout.visibility=View.VISIBLE
                    binding.favList.visibility=View.GONE
                }else{
                    favListAdapter.setItems(it)
                    binding.noListLayout.visibility=View.GONE
                    binding.favList.visibility=View.VISIBLE
                }

            }
        }


    }

    private fun setToolBar() {
        if (viewModel.checkedLabelList.value!=null){
            binding.favToolBar.menu.setGroupVisible(R.id.all_fav,false)
            binding.favToolBar.menu.setGroupVisible(R.id.label_fav,true)
            binding.favToolBar.title= viewModel.labelsList.value.first { label -> label.id== viewModel.checkedLabelList.value!![0] }.label

        }else{
            binding.favToolBar.title=resources.getString(R.string.favorites)
            binding.favToolBar.menu.setGroupVisible(R.id.all_fav,true)
            binding.favToolBar.menu.setGroupVisible(R.id.label_fav,false)
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
                viewModel.searchQuery.value=newText

                return false
            }
        })
        searchView.setOnFocusChangeListener {
                _, hasFocus->
            if (hasFocus){
                binding.noList.text=resources.getString(R.string.no_results)
            }else {
                binding.noList.text=resources.getString(R.string.no_favorites_found)
                viewModel.searchQuery.value=null
            }

        }
        binding.favToolBar.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.delete_all -> {
                    viewModel.deleteAllFav()
                    true
                }
                R.id.clear_all -> {
                    viewModel.clearAllFav()
                    true
                }
                R.id.delete_label -> {
                    viewModel.deleteLabel()
                    true
                }
                R.id.clear_label -> {
                    viewModel.clearLabel()
                    true
                }
                R.id.rename_label -> {
                    addLabelDialog(requireContext(),viewModel.checkedLabelList.value!![0])
                    true
                }
            else -> false
            }


        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

     private fun addLabelDialog(context: Context,id:Int =0) {

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

                viewModel.addLabel(editText.text!!.trim().toString(),id)
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

        if (checked){
            viewModel.checkedLabelList.value= arrayListOf(label.id)
            binding.favToolBar.title=label.label
            binding.favToolBar.menu.setGroupVisible(R.id.all_fav,false)
            binding.favToolBar.menu.setGroupVisible(R.id.label_fav,true)
        }else{
            viewModel.checkedLabelList.value=null
            binding.favToolBar.title=resources.getString(R.string.favorites)
            binding.favToolBar.menu.setGroupVisible(R.id.all_fav,true)
            binding.favToolBar.menu.setGroupVisible(R.id.label_fav,false)
        }


    }

    override fun onPlayClicked(video: FavVideo) {
        startService()
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(KeyText.urlPrefix + video.id)))
    }

    override fun onFavClicked(videoTable: FavVideo, selected: Boolean) {
        if (selected) {
            videoTable.isFav = true
            viewModel.insertFAv(videoTable)
        }else viewModel.deleteFav(videoTable)
    }

    override fun onChannelClicked(video: FavVideo) {
        startService()
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video.channelUrl)))
    }

    override fun onShareOptionClicked(video: FavVideo) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody = "Look what I found @NavTube " + KeyText.urlPrefix + video.id
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    override fun onMoveItemClicked(video: FavVideo) {
        if (!viewModel.labelsList.value.isNullOrEmpty() ) {

            viewModel.labelsList.value?.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.selectLabel))
                    .setItems(it.map { label -> label.label }.toTypedArray()) { dialog, which ->
                        video.label = it[which].id
                        viewModel.updateFavVideo(video)
                    }
                    .show()
            }
        }else makeMessage(resources.getString(R.string.no_label_make_label))
    }

    private fun makeMessage(message: String) {
        Snackbar.make(requireContext(),binding.root,message,1000).show()
    }
    private fun startService(){
        binding.root.postDelayed({
            val service = Intent(context, BubbleService::class.java)
            ContextCompat.startForegroundService(requireContext(),service)
        },2000)

    }
}