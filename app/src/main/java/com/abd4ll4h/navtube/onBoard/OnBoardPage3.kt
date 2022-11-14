package com.abd4ll4h.navtube.onBoard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import com.abd4ll4h.navtube.R
import com.abd4ll4h.navtube.databinding.FragmentOnBoardPage3Binding

class OnBoardPage3 : Fragment() {

    private lateinit var _binding:FragmentOnBoardPage3Binding
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding= FragmentOnBoardPage3Binding.inflate(inflater,container,false)
        if (Settings.canDrawOverlays(requireContext()))binding.button.visibility=GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val intent =
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${requireActivity().packageName}"))

        binding.button.setOnClickListener(){
            startActivityForResult(intent, 101)
        }
    }
}