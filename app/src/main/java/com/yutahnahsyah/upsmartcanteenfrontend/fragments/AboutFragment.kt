package com.yutahnahsyah.upsmartcanteenfrontend.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yutahnahsyah.upsmartcanteenfrontend.R

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<View>(R.id.toolbar)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Navigate to Support screen when "Contact Support" is clicked
        val layoutContactSupport = view.findViewById<LinearLayout>(R.id.layoutContactSupport)
        layoutContactSupport.setOnClickListener {
            findNavController().navigate(R.id.action_nav_about_to_nav_support)
        }
    }
}
