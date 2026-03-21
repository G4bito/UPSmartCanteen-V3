package com.yutahnahsyah.upsmartcanteenfrontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.FoodItem
import java.util.Locale

class CartItemAdapter(
    private var items: List<FoodItem>,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.Adapter<CartItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodImage: ImageView = view.findViewById(R.id.ivFoodImage)
        val foodName: TextView = view.findViewById(R.id.tvFoodName)
        val stallName: TextView = view.findViewById(R.id.tvStallName)
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
        holder.stallName.text = food.stall_name ?: "Unknown Stall"
        holder.foodPrice.text = String.format(Locale.getDefault(), "₱%.2f", food.price)
        
        // Use quantity from the backend model if available, otherwise default to 1
        holder.tvQuantity.text = "1"

        val serverUrl = "http://192.168.68.113:3000"
        val imageUrl = if (!food.image_url.isNullOrEmpty()) {
            val cleanPath = food.image_url.trim().removePrefix("/")
            "$serverUrl/$cleanPath"
        } else {
            null
        }

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.food_image)
            .into(holder.foodImage)

        // Since this is just for display in the cart, 
        // logic for updating quantity on backend can be added here
    }

    override fun getItemCount() = items.size

    fun updateData(newList: List<FoodItem>) {
        this.items = newList
        notifyDataSetChanged()
    }
    
    fun getItems(): List<FoodItem> = items
}