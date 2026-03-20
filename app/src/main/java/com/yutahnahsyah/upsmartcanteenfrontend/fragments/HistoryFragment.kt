package com.yutahnahsyah.upsmartcanteenfrontend.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.adapter.OrderHistoryAdapter
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.Order

class HistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<View>(R.id.toolbar)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val rvHistory = view.findViewById<RecyclerView>(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(requireContext())

        // Dummy data for order history
        val orders = listOf(
            Order("12345", "17 May 2024", "Completed", 150.00, "- 1x Fried Chicken\n- 1x Rice\n- 1x Coke"),
            Order("12346", "16 May 2024", "Completed", 85.00, "- 1x Burger\n- 1x Fries"),
            Order("12347", "15 May 2024", "Completed", 45.00, "- 1x Sisig Rice Bowl")
        )

        val adapter = OrderHistoryAdapter(orders)
        rvHistory.adapter = adapter
    }
}
