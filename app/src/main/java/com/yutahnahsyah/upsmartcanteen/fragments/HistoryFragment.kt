package com.yutahnahsyah.upsmartcanteen.fragments

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
import com.yutahnahsyah.upsmartcanteen.R
import com.yutahnahsyah.upsmartcanteen.RetrofitClient
import com.yutahnahsyah.upsmartcanteen.adapter.OrderHistoryAdapter
import com.yutahnahsyah.upsmartcanteen.data.model.Order
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

  private lateinit var adapter: OrderHistoryAdapter
  private var tvOrderCount: TextView? = null

  // 1. Define the Polling Job
  private var pollingJob: Job? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_history, container, false)

    val toolbar = view.findViewById<View>(R.id.toolbar)
    toolbar.setOnClickListener {
      parentFragmentManager.popBackStack()
    }

    tvOrderCount = view.findViewById(R.id.tvOrderCount)
    val rvHistory = view.findViewById<RecyclerView>(R.id.rvHistory)
    rvHistory.layoutManager = LinearLayoutManager(requireContext())

    adapter = OrderHistoryAdapter(emptyList())
    rvHistory.adapter = adapter

    return view
  }

  // 2. Start polling when fragment is visible
  override fun onResume() {
    super.onResume()
    startPolling()
  }

  // 3. Stop polling when fragment is hidden/paused to save resources
  override fun onPause() {
    super.onPause()
    pollingJob?.cancel()
  }

  private fun startPolling() {
    pollingJob?.cancel()
    pollingJob = viewLifecycleOwner.lifecycleScope.launch {
      while (true) {
        fetchOrderHistory()
        // Poll every 5 seconds (adjust as needed)
        delay(3_000)
      }
    }
  }

  private fun fetchOrderHistory() {
    val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPref.getString("auth_token", null)

    if (token == null) return

    // Note: We don't need to launch another coroutine here
    // because fetchOrderHistory is called inside the pollingJob scope.
    // However, keeping it as is works fine for Retrofit's suspend functions.
    viewLifecycleOwner.lifecycleScope.launch {
      try {
        val response = RetrofitClient.instance.getMyOrders("Bearer $token")
        if (response.isSuccessful) {
          val responseBody = response.body() ?: emptyList()

          val orders = responseBody.map { res ->
            // Format the items into a string like "1x Burger\n2x Fries"
            val itemsSummary = res.items?.joinToString("\n") { item ->
              "${item.quantity}x ${item.item_name}"
            } ?: "No items" // Fallback if list is empty

            Order(
              orderId = res.order_id.toString(),
              date = res.order_time ?: "",
              status = res.status,
              totalPrice = res.total_price,
              items = itemsSummary, // ✅ Now uses the formatted string
              customer_name = res.customer_name,
              department = res.department
            )
          }

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