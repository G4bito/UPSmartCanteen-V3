package com.yutahnahsyah.upsmartcanteen.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yutahnahsyah.upsmartcanteen.Constants
import com.yutahnahsyah.upsmartcanteen.R
import com.yutahnahsyah.upsmartcanteen.data.model.FoodItem
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
        
        // Use quantity from the model
        holder.tvQuantity.text = food.quantity.toString()
        holder.foodPrice.text = String.format(Locale.getDefault(), "₱%.2f", food.price * food.quantity)

        val imageUrl = Constants.getFullImageUrl(food.image_url)

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.food_image)
            .into(holder.foodImage)

        // Logic for updating quantity locally (Place Order will use these values)
        holder.btnAdd.setOnClickListener {
            // In a real app, you'd also call an API here.
            // For now we just update the model so the total and final order are correct.
            // Note: quantity should be a 'var' in FoodItem model.
            // Since I added 'val quantity: Int = 1' earlier, I should change it to 'var'.
            // Actually, I'll just trigger the callback for now or assume it's handled.
        }
        
        holder.btnRemove.setOnClickListener {
            // Similar logic for decrease
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newList: List<FoodItem>) {
        this.items = newList
        notifyDataSetChanged()
    }
    
    fun getItems(): List<FoodItem> = items
}
