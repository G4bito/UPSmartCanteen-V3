package com.yutahnahsyah.upsmartcanteenfrontend.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.RetrofitClient
import com.yutahnahsyah.upsmartcanteenfrontend.adapter.CartItemAdapter
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.FoodItem
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.util.Locale

class CartDetailsFragment : Fragment() {

    private val args: CartDetailsFragmentArgs by navArgs()
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTotal: TextView
    private lateinit var rvCartItems: RecyclerView
    private lateinit var adapter: CartItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val tvStoreNameHeader = view.findViewById<TextView>(R.id.tvStoreNameHeader)
        rvCartItems = view.findViewById(R.id.rvCartItems)
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvTotal = view.findViewById(R.id.tvTotal)
        val btnConfirmPayment = view.findViewById<MaterialButton>(R.id.btnConfirmPayment)

        tvStoreNameHeader.text = args.storeName

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        rvCartItems.layoutManager = LinearLayoutManager(requireContext())

        adapter = CartItemAdapter(emptyList()) {
            // Re-calculate totals when quantity changes
            updateTotals(adapter.getItems())
        }
        rvCartItems.adapter = adapter

        fetchCartItems()

        btnConfirmPayment.setOnClickListener {
            val action = CartDetailsFragmentDirections.actionNavCartDetailsToNavPayment()
            findNavController().navigate(action)
        }
    }

    private fun fetchCartItems() {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)

        if (token == null) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMyCart("Bearer $token")
                if (response.isSuccessful) {
                    val allItems = response.body() ?: emptyList()
                    // Filter items only for this specific stall
                    val stallItems = allItems.filter { it.stall_name == args.storeName }
                    
                    adapter.updateData(stallItems)
                    updateTotals(stallItems)
                } else {
                    Log.e("CART_FETCH", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("CART_FETCH", "Exception", e)
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTotals(items: List<FoodItem>) {
        val total = items.sumOf { it.price }
        tvSubtotal.text = String.format(Locale.getDefault(), "₱%.2f", total)
        tvTotal.text = String.format(Locale.getDefault(), "₱%.2f", total)
    }
}
