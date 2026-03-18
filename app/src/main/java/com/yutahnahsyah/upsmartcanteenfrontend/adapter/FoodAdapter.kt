package com.yutahnahsyah.upsmartcanteenfrontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.FoodItem
import java.util.Locale

class FoodAdapter(
    private var fullList: List<FoodItem>,
    private val onClick: (FoodItem) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    private var displayedList: MutableList<FoodItem> = fullList.toMutableList()

    class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.foodName)
        val price: TextView = view.findViewById(R.id.foodPrice)
        val store: TextView = view.findViewById(R.id.foodStore)
        val image: ImageView = view.findViewById(R.id.foodImage)
        val description: TextView = view.findViewById(R.id.foodDescription)
        val stock: TextView = view.findViewById(R.id.foodStock)
        val status: TextView = view.findViewById(R.id.foodStatus)
        val category: TextView = view.findViewById(R.id.foodCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = displayedList[position]

        holder.name.text = food.name
        holder.price.text = String.format(Locale.getDefault(), "₱%.2f", food.price)
        
        holder.store.text = food.stall_name ?: "Unknown Stall"
        holder.category.text = food.category
        holder.description.text = food.description ?: "No description provided."
        holder.stock.text = "Stocks: ${food.stock_qty}"

        if (food.is_available && food.stock_qty > 0) {
            holder.status.text = "Available"
            holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
        } else {
            holder.status.text = if (food.stock_qty <= 0) "Out of Stock" else "Unavailable"
            holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
        }

        val serverUrl = "http://192.168.18.41:3000"
        val imageUrl = if (!food.image_url.isNullOrEmpty()) {
            val cleanPath = food.image_url.trim().removePrefix("/")
            "$serverUrl/$cleanPath"
        } else {
            null
        }

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.food_image)
            .error(R.drawable.food_image)
            .into(holder.image)

        holder.itemView.setOnClickListener { onClick(food) }
    }

    override fun getItemCount() = displayedList.size

    fun getItemList(): List<FoodItem> {
        return displayedList
    }

    fun updateData(newList: List<FoodItem>) {
        this.fullList = newList
        this.displayedList = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun filterByCategory(category: String) {
        displayedList.clear()
        if (category == "All") {
            displayedList.addAll(fullList)
        } else {
            val filtered = fullList.filter { 
                // Using startsWith to match "Dessert" with "Desserts" in the database
                it.category.startsWith(category, ignoreCase = true)
            }
            displayedList.addAll(filtered)
        }
        notifyDataSetChanged()
    }

    fun filterBySearch(query: String) {
        displayedList.clear()
        if (query.isEmpty()) {
            displayedList.addAll(fullList)
        } else {
            val filtered = fullList.filter {
                it.name.contains(query, ignoreCase = true)
            }
            displayedList.addAll(filtered)
        }
        notifyDataSetChanged()
    }
}