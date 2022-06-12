package com.abd4ll4h.navtube

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.abd4ll4h.navtube.DataFetch.Response
import com.abd4ll4h.navtube.DataFetch.VideoTable
import com.abd4ll4h.navtube.DataFetch.scraper.keyText.urlPrefix
import com.abd4ll4h.navtube.adapters.MainListAdapter
import com.abd4ll4h.navtube.databinding.FragmentMainBinding
import com.abd4ll4h.navtube.viewModel.MainFragmentViewModel
import kotlinx.coroutines.launch


class MainFragment : Fragment(), MainListAdapter.ItemClick {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    val viewModel: MainFragmentViewModel =
        ViewModelProvider.AndroidViewModelFactory(requireActivity().application).create(MainFragmentViewModel::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        val listAdapter=MainListAdapter(requireContext().applicationContext,ArrayList<VideoTable>(),this)

        binding.mainList.adapter=listAdapter
        binding.mainList.layoutManager= LinearLayoutManager(requireContext())
        binding.mainList.setHasFixedSize(false)
        binding.mainList.addItemDecoration(MarginItemDecoration(8))
        viewLifecycleOwner.lifecycleScope.launch  {
            viewModel.getVideoItem().observe(viewLifecycleOwner) {
                if (it!!.status == Response.Status.SUCCESS)
                {
                    listAdapter.setList(it.data)
                }else{
                    Toast.makeText(context,it.message,Toast.LENGTH_LONG).show()
                    Log.i("faild@check","massge:"+it.message.toString())
                }

            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setOnMenuItemClicked()
        super.onViewCreated(view, savedInstanceState)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onChannelClicked(video: VideoTable) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video.channelUrl)))
    }

    override fun onPlayClicked(video: VideoTable) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlPrefix+video.id)))
    }

    override fun onShareOptionClicked(video: VideoTable) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody = "Look what I found @NavTube "+urlPrefix+video.id
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    override fun onReportOptionClicked(video: Int) {
        Toast.makeText(context, "Soon", Toast.LENGTH_LONG).show()
    }
}