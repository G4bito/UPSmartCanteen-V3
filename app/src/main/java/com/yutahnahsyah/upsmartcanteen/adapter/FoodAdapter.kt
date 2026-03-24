package com.yutahnahsyah.upsmartcanteen.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yutahnahsyah.upsmartcanteen.Constants
import com.yutahnahsyah.upsmartcanteen.R
import com.yutahnahsyah.upsmartcanteen.data.model.FoodItem
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
        val statusDot: View = view.findViewById(R.id.statusDot)
        val btnAddToCart: CardView = view.findViewById(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

  override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
    val food = displayedList[position]
    val ctx = holder.itemView.context

    // 1. Basic fields setup
    holder.name.text = food.name
    holder.price.text = String.format(Locale.getDefault(), "₱%.2f", food.price)
    holder.store.text = food.stall_name ?: "Unknown Stall"
    holder.category.text = food.category.uppercase()
    holder.description.text = food.description ?: "No description provided."
    holder.stock.text = "Stocks: ${food.stock_qty}"

    // 2. Combined Availability Logic
    // Check if the item is active, has stock, AND the stall is open
    val isStallOpen = food.is_open // Ensure your FoodItem data class has this field
    val hasStock = food.stock_qty > 0
    val isItemActive = food.is_available

    val canOrder = isStallOpen && hasStock && isItemActive

    if (canOrder) {
      // --- OPEN / AVAILABLE STATE ---
      holder.statusDot.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_dot_open)
      holder.status.text = "Available"
      holder.status.setTextColor(Color.parseColor("#43A047"))

      holder.btnAddToCart.setCardBackgroundColor(Color.parseColor("#1B5E20"))
      holder.btnAddToCart.isEnabled = true
      holder.btnAddToCart.alpha = 1f

      // Make sure the whole item is clickable if the stall is open
      holder.itemView.isClickable = true
      holder.itemView.alpha = 1f
    } else {
      // --- CLOSED / UNAVAILABLE STATE ---
      holder.statusDot.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_dot_closed)
      holder.status.setTextColor(Color.parseColor("#E53935"))

      // Determine the specific label to show
      holder.status.text = when {
        !isStallOpen -> "Stall Closed"
        food.stock_qty <= 0 -> "Out of Stock"
        else -> "Unavailable"
      }

      // Disable interaction
      holder.btnAddToCart.setCardBackgroundColor(Color.parseColor("#E0E0E0"))
      holder.btnAddToCart.isEnabled = false
      holder.btnAddToCart.alpha = 0.5f

      // Disable the whole card click if the stall is closed
      holder.itemView.isClickable = false
      holder.itemView.alpha = 0.8f // Slightly dim the whole card to show it's disabled
    }

    // 3. Image loading
    val imageUrl = Constants.getFullImageUrl(food.image_url)
    Glide.with(ctx)
      .load(imageUrl)
      .placeholder(R.drawable.food_image)
      .error(R.drawable.food_image)
      .into(holder.image)

    // 4. Click listeners (Respect the 'canOrder' state)
    holder.itemView.setOnClickListener {
      if (canOrder) onClick(food)
    }

    holder.btnAddToCart.setOnClickListener {
      if (canOrder) onClick(food)
    }
  }

    override fun getItemCount() = displayedList.size

    fun getItemList(): List<FoodItem> = displayedList

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
            displayedList.addAll(fullList.filter {
                it.category.startsWith(category, ignoreCase = true)
            })
        }
        notifyDataSetChanged()
    }

    fun filterBySearch(query: String) {
        displayedList.clear()
        if (query.isEmpty()) {
            displayedList.addAll(fullList)
        } else {
            displayedList.addAll(fullList.filter {
                it.name.contains(query, ignoreCase = true)
            })
        }
        notifyDataSetChanged()
    }
}
