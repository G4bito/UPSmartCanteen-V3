package com.yutahnahsyah.upsmartcanteenfrontend.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.RetrofitClient
import com.yutahnahsyah.upsmartcanteenfrontend.adapter.OrderHistoryAdapter
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private lateinit var adapter: OrderHistoryAdapter
    private var tvOrderCount: TextView? = null

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

        tvOrderCount = view.findViewById(R.id.tvOrderCount)
        val rvHistory = view.findViewById<RecyclerView>(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(requireContext())

        adapter = OrderHistoryAdapter(emptyList())
        rvHistory.adapter = adapter

        fetchOrderHistory()
    }

    private fun fetchOrderHistory() {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)

        if (token == null) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMyOrders("Bearer $token")
                if (response.isSuccessful) {
                    val orders = response.body() ?: emptyList()
                    adapter.updateData(orders)
                    tvOrderCount?.text = "${orders.size} orders"
                } else {
                    Log.e("HISTORY_FETCH", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("HISTORY_FETCH", "Exception", e)
            }
        }
    }
}
