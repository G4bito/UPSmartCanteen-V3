package com.yutahnahsyah.upsmartcanteenfrontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.Food

class FoodAdapter(
    private val fullList: List<Food>,    // Renamed to 'fullList' to avoid confusion
    private val onClick: (Food) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    // This is the list the RecyclerView actually "sees"
    private var displayedList: MutableList<Food> = fullList.toMutableList()

    class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.foodName)
        val price: TextView = view.findViewById(R.id.foodPrice)
        val store: TextView = view.findViewById(R.id.foodStore)
        val image: ImageView = view.findViewById(R.id.foodImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        // Kept your layout name 'item_food'
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        // IMPORTANT: Use 'displayedList' here, not 'fullList'
        val food = displayedList[position]

        holder.name.text = food.name
        holder.price.text = "₱${food.price}"
        holder.store.text = food.store
        holder.image.setImageResource(food.imageRes)

        holder.itemView.setOnClickListener { onClick(food) }
    }

    // Return the size of the FILTERED list
    override fun getItemCount() = displayedList.size

    // --- NEW: Filter by Category ("Meals", "Snacks") ---
    fun filterByCategory(category: String) {
        displayedList.clear()
        if (category == "All") {
            displayedList.addAll(fullList)
        } else {
            // This requires the 'category' field in your Food.kt!
            val filtered = fullList.filter { it.category.equals(category, ignoreCase = true) }
            displayedList.addAll(filtered)
        }
        notifyDataSetChanged()
    }

    // --- NEW: Filter by Search Text ---
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