package com.yutahnahsyah.upsmartcanteenfrontend.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.Store
import com.yutahnahsyah.upsmartcanteenfrontend.adapter.StoreAdapter

class StallFragment : Fragment() {

  private lateinit var adapter: StoreAdapter

  private val storeList = listOf(
    Store("Store Number One", "Category One", R.drawable.store_image),
    Store("Store Number Two", "Category Two", R.drawable.store_image),
    Store("Store Number Three", "Category Three", R.drawable.store_image)
  )

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

    adapter = StoreAdapter(storeList) { selectedStore ->
      val bundle = Bundle().apply {
        putString("storeName", selectedStore.name)
      }
      findNavController().navigate(R.id.action_nav_stall_to_nav_store_food, bundle)
    }

    rv.layoutManager = LinearLayoutManager(requireContext())
    rv.adapter = adapter

    // Implement real-time search filtering
    searchBar.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        adapter.filter(s.toString())
      }
      override fun afterTextChanged(s: Editable?) {}
    })
  }
}
