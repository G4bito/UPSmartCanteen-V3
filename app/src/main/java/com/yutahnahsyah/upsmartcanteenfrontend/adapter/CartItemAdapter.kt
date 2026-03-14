package com.yutahnahsyah.upsmartcanteenfrontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.Food
import java.util.Locale

class CartItemAdapter(
    private val items: MutableList<Food>,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.Adapter<CartItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodImage: ImageView = view.findViewById(R.id.ivFoodImage)
        val foodName: TextView = view.findViewById(R.id.tvFoodName)
        val foodOptions: TextView = view.findViewById(R.id.tvOptions)
        val foodPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val btnAdd: ImageView = view.findViewById(R.id.btnAdd)
        val btnRemove: ImageView = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart_food, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val food = items[position]
        
        holder.foodName.text = food.name
        holder.foodPrice.text = String.format(Locale.getDefault(), "₱%.2f", food.price.toDouble())
        holder.foodImage.setImageResource(food.imageRes)
        holder.foodOptions.text = "Regular" // Default placeholder
        
        // Quantity logic (for mock UI)
        var quantity = 1
        holder.tvQuantity.text = quantity.toString()

        holder.btnAdd.setOnClickListener {
            quantity++
            holder.tvQuantity.text = quantity.toString()
            onQuantityChanged()
        }

        holder.btnRemove.setOnClickListener {
            if (quantity > 1) {
                quantity--
                holder.tvQuantity.text = quantity.toString()
                onQuantityChanged()
            }
        }
    }

    override fun getItemCount() = items.size
}