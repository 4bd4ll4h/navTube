package com.abd4ll4h.navtube

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.abd4ll4h.navtube.DataFetch.Scraper
import com.abd4ll4h.navtube.databinding.FragmentMainBinding


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val  recyclerView= lazy { binding.mainList }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnMenuItemClicked()

        val scraper =Scraper(requireContext())

        }

    private fun setOnMenuItemClicked() {
        binding.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_settings -> {
                    Toast.makeText(context,"Soon",Toast.LENGTH_LONG).show()
                    true
                }
                R.id.mainFilter -> {
                    Toast.makeText(context,"Soon",Toast.LENGTH_LONG).show()
                    true
                }
                else -> false
            }

        }
        binding.toolBar.menu.getItem(0).actionView.setOnClickListener {
            Toast.makeText(context,"Soon",Toast.LENGTH_LONG).show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}