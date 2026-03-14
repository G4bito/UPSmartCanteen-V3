package com.yutahnahsyah.upsmartcanteenfrontend.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.adapter.CartItemAdapter
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.Food
import com.google.android.material.button.MaterialButton
import java.util.Locale

class CartDetailsFragment : Fragment() {

    private val args: CartDetailsFragmentArgs by navArgs()
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTotal: TextView
    private var cartItems = mutableListOf<Food>()

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
        val rvCartItems = view.findViewById<RecyclerView>(R.id.rvCartItems)
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvTotal = view.findViewById(R.id.tvTotal)
        val btnConfirmPayment = view.findViewById<MaterialButton>(R.id.btnConfirmPayment)

        tvStoreNameHeader.text = args.storeName

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Mock data for the specific store
        cartItems = mutableListOf(
            Food("Banana Cue", 20, args.storeName, R.drawable.food_image, "Snacks"),
            Food("Turon", 15, args.storeName, R.drawable.food_image, "Snacks"),
            Food("Coke", 25, args.storeName, R.drawable.food_image, "Drinks")
        )

        val adapter = CartItemAdapter(cartItems) {
            updateTotals()
        }
        rvCartItems.layoutManager = LinearLayoutManager(requireContext())
        rvCartItems.adapter = adapter

        updateTotals()

        btnConfirmPayment.setOnClickListener {
            val action = CartDetailsFragmentDirections.actionNavCartDetailsToNavPayment()
            findNavController().navigate(action)
        }
    }

    private fun updateTotals() {
        val total = cartItems.sumOf { it.price.toDouble() }
        
        tvSubtotal.text = String.format(Locale.getDefault(), "₱%.2f", total)
        tvTotal.text = String.format(Locale.getDefault(), "₱%.2f", total)
    }
}