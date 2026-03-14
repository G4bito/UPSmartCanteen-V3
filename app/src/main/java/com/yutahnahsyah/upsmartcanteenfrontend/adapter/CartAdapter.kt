package com.yutahnahsyah.upsmartcanteenfrontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.Cart

class CartAdapter(
  private val items: List<Cart>,
  private val onClick: (Cart) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

  class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val name: TextView = view.findViewById(R.id.name)
    val status: TextView = view.findViewById(R.id.status)
    val price: TextView = view.findViewById(R.id.price)
    val image1: ImageView = view.findViewById(R.id.imageRes1)
    val image2: ImageView = view.findViewById(R.id.imageRes2)
    val btnViewCart: MaterialButton = view.findViewById(R.id.btnViewCart)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
    return CartViewHolder(view)
  }

  override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
    val item = items[position]
    holder.name.text = item.name
    holder.status.text = item.status
    holder.price.text = "₱${item.price}"
    holder.image1.setImageResource(item.imageRes1)
    holder.image2.setImageResource(item.imageRes2)

    holder.btnViewCart.setOnClickListener { onClick(item) }
  }

  override fun getItemCount() = items.size
}