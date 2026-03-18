package com.yutahnahsyah.upsmartcanteenfrontend.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.RetrofitClient
import com.yutahnahsyah.upsmartcanteenfrontend.adapter.StallCartAdapter
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.StallCart
import kotlinx.coroutines.launch

class CartFragment : Fragment() {

    private lateinit var adapter: StallCartAdapter
    private lateinit var rvCart: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCart = view.findViewById(R.id.recyclerView)
        rvCart.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = StallCartAdapter(emptyList()) { selectedStallCart ->
            // Navigate to details for this specific stall's cart
            val action = CartFragmentDirections.actionNavCartToNavCartDetails(selectedStallCart.stallName)
            findNavController().navigate(action)
        }
        rvCart.adapter = adapter

        fetchCartItems()
    }

    private fun fetchCartItems() {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)

        if (token == null) {
            Toast.makeText(requireContext(), "Please login to view cart", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMyCart("Bearer $token")
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    
                    // Group items by stall_name to match the Foodpanda "All carts" UI
                    val groupedCarts = items.groupBy { it.stall_name ?: "Unknown Stall" }
                        .map { (stallName, foodItems) ->
                            StallCart(
                                stallName = stallName,
                                items = foodItems,
                                subtotal = foodItems.sumOf { it.price }
                            )
                        }
                    
                    adapter.updateData(groupedCarts)
                } else {
                    Log.e("CART_FETCH", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("CART_FETCH", "Exception", e)
                Toast.makeText(requireContext(), "Failed to fetch cart", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
