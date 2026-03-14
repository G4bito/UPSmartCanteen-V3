package com.yutahnahsyah.upsmartcanteenfrontend.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.Cart
import com.yutahnahsyah.upsmartcanteenfrontend.adapter.CartAdapter


class CartFragment : Fragment() {

  private lateinit var adapter: CartAdapter

  private val cartList = listOf(
    Cart(
      "Main Canteen",
      "Processing Order...",
      145.0,
      R.drawable.food_image,
      R.drawable.food_image
    ),
    Cart(
      "Annex Food Court",
      "Ready for Pickup",
      85.0,
      R.drawable.food_image,
      R.drawable.food_image
    )
  )

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_cart, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val rv = view.findViewById<RecyclerView>(R.id.recyclerView)

    adapter = CartAdapter(cartList) { selectedCart ->
      val action = CartFragmentDirections.actionNavCartToNavCartDetails(selectedCart.name)
      findNavController().navigate(action)
    }

    rv.layoutManager = LinearLayoutManager(requireContext())
    rv.adapter = adapter
  }
}