package com.yutahnahsyah.upsmartcanteen.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteen.R
import com.yutahnahsyah.upsmartcanteen.RetrofitClient
import com.yutahnahsyah.upsmartcanteen.adapter.StoreAdapter
import kotlinx.coroutines.launch

class StallFragment : Fragment() {

  private lateinit var adapter: StoreAdapter
  private var tvOpenCount: TextView? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_stall, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val rv = view.findViewById<RecyclerView>(R.id.recyclerView)
    val searchBar = view.findViewById<EditText>(R.id.searchBar)
    tvOpenCount = view.findViewById(R.id.tvOpenCount)

    adapter = StoreAdapter(emptyList()) { selectedStall ->
      if (selectedStall.is_active) {
        val bundle = Bundle().apply {
          putString("storeName", selectedStall.stall_name)
          putInt("stallId", selectedStall.stall_id)
        }
        findNavController().navigate(R.id.action_nav_stall_to_nav_store_food, bundle)
      } else {
        Toast.makeText(requireContext(), "${selectedStall.stall_name} is currently closed", Toast.LENGTH_SHORT).show()
      }
    }

    rv.layoutManager = LinearLayoutManager(requireContext())
    rv.adapter = adapter

    fetchStalls()

    // Implement real-time search filtering
    searchBar.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        adapter.filter(s.toString())
      }
      override fun afterTextChanged(s: Editable?) {}
    })
  }

  private fun fetchStalls() {
    viewLifecycleOwner.lifecycleScope.launch {
      try {
        val response = RetrofitClient.instance.getStalls()
        if (response.isSuccessful) {
          val allStalls = response.body() ?: emptyList()
          
          // Update the open count chip
          val openCount = allStalls.count { it.is_active }
          tvOpenCount?.text = "$openCount open"
          
          // Show all stalls, not just active ones
          adapter.updateData(allStalls)
        } else {
          Log.e("STALL_FETCH", "Error: ${response.code()}")
          Toast.makeText(requireContext(), "Failed to fetch stalls", Toast.LENGTH_SHORT).show()
        }
      } catch (e: Exception) {
        Log.e("STALL_FETCH", "Exception", e)
        Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
      }
    }
  }
}
