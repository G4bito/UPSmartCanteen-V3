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
import com.yutahnahsyah.upsmartcanteenfrontend.PlaceOrderRequest
import kotlinx.coroutines.launch
import java.util.Locale

class CartDetailsFragment : Fragment() {

    private val args: CartDetailsFragmentArgs by navArgs()
    private var tvSubtotal: TextView? = null
    private var tvTotal: TextView? = null
    private var tvTotalBottom: TextView? = null
    private var tvItemCount: TextView? = null
    private var rvCartItems: RecyclerView? = null
    private var adapter: CartItemAdapter? = null
    private var currentStallItems: List<FoodItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<View>(R.id.btnBack)
        val tvStoreNameHeader = view.findViewById<TextView>(R.id.tvStoreNameHeader)
        val btnAddMore = view.findViewById<View>(R.id.btnAddMore)
        val btnPlaceOrder = view.findViewById<MaterialButton>(R.id.btnConfirmPayment)

        rvCartItems = view.findViewById(R.id.rvCartItems)
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvTotal = view.findViewById(R.id.tvTotal)
        tvTotalBottom = view.findViewById(R.id.tvTotalBottom)
        tvItemCount = view.findViewById(R.id.tvItemCount)

        tvStoreNameHeader?.text = args.storeName

        btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        btnAddMore?.setOnClickListener {
            findNavController().navigateUp()
        }

        rvCartItems?.layoutManager = LinearLayoutManager(requireContext())

        adapter = CartItemAdapter(emptyList()) {
            updateTotals(adapter?.getItems() ?: emptyList())
        }
        rvCartItems?.adapter = adapter

        fetchCartItems()

        btnPlaceOrder?.setOnClickListener {
            if (currentStallItems.isNotEmpty()) {
                placeOrder()
            } else {
                Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show()
            }
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
                    currentStallItems = allItems.filter { it.stall_name == args.storeName }

                    adapter?.updateData(currentStallItems)
                    updateTotals(currentStallItems)
                } else {
                    Log.e("CART_DETAILS", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("CART_DETAILS", "Exception", e)
            }
        }
    }

    private fun placeOrder() {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)

        if (token == null) return

        val stallId = currentStallItems.firstOrNull()?.stall_id ?: 0
        val request = PlaceOrderRequest(
            stall_id = stallId,
            payment_type = "cash", // Corrected to lowercase "cash"
            order_remarks = ""
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.placeOrder("Bearer $token", request)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Order placed successfully!", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_nav_cart_details_to_nav_history)
                } else {
                    val errorMsg = response.errorBody()?.string()
                    Log.e("PLACE_ORDER", "Error: $errorMsg")
                    Toast.makeText(requireContext(), "Failed to place order", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PLACE_ORDER", "Exception", e)
                Toast.makeText(requireContext(), "Connection error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTotals(items: List<FoodItem>) {
        val total = items.sumOf { it.price * it.quantity }
        val priceString = String.format(Locale.getDefault(), "₱%.2f", total)

        tvSubtotal?.text = priceString
        tvTotal?.text = priceString
        tvTotalBottom?.text = priceString
        tvItemCount?.text = "${items.size} items"
    }
}