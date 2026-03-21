package com.yutahnahsyah.upsmartcanteen.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.yutahnahsyah.upsmartcanteen.R

class SupportFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_support, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<View>(R.id.toolbar)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Changed from MaterialCardView to View to avoid ClassCastException (Layout uses LinearLayout)
        view.findViewById<View>(R.id.btnContactEmail).setOnClickListener {
            Toast.makeText(context, "Opening email client...", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btnContactPhone).setOnClickListener {
            Toast.makeText(context, "Opening dialer...", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<MaterialButton>(R.id.btnReportIssue).setOnClickListener {
            Toast.makeText(context, "Report form coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}
