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
    private var items: MutableList<FoodItem>,
    private val onQuantityChanged: () -> Unit,
    private val onItemRemoved: (FoodItem) -> Unit,
    private val onQuantityUpdated: (cartItemId: Int, quantity: Int) -> Unit
  ) : RecyclerView.Adapter<CartItemAdapter.ViewHolder>() {

    private val unavailableItemIds = mutableSetOf<Int>()  // ← track unavailable items

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
      val foodImage: ImageView = view.findViewById(R.id.ivFoodImage)
      val foodName: TextView = view.findViewById(R.id.tvFoodName)
      val stallName: TextView = view.findViewById(R.id.tvStallName)
      val foodPrice: TextView = view.findViewById(R.id.tvPrice)
      val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
      val btnAdd: ImageView = view.findViewById(R.id.btnAdd)
      val btnRemove: ImageView = view.findViewById(R.id.btnRemove)
      val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_cart_food, parent, false)
      return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val food = items[position]
      val isUnavailable = unavailableItemIds.contains(food.item_id)

      holder.foodName.text = food.name
      holder.stallName.text = food.stall_name ?: "Unknown Stall"
      holder.tvQuantity.text = food.quantity.toString()
      holder.foodPrice.text = String.format(
        Locale.getDefault(), "₱%.2f", food.price * food.quantity
      )

      // ✅ Grey out unavailable items
      val alpha = if (isUnavailable) 0.4f else 1.0f
      holder.foodImage.alpha = alpha
      holder.foodName.alpha = alpha
      holder.stallName.alpha = alpha
      holder.foodPrice.alpha = alpha
      holder.tvQuantity.alpha = alpha
      holder.btnAdd.alpha = alpha
      holder.btnRemove.alpha = alpha

      // ✅ Show unavailable label
      holder.stallName.text = if (isUnavailable) {
        "Unavailable — please remove to place order"
      } else {
        food.stall_name ?: "Unknown Stall"
      }
      holder.stallName.setTextColor(
        if (isUnavailable) android.graphics.Color.parseColor("#E53935")
        else android.graphics.Color.parseColor("#90A4AE")
      )

      val imageUrl = Constants.getFullImageUrl(food.image_url)
      Glide.with(holder.itemView.context)
        .load(imageUrl)
        .placeholder(R.drawable.food_image)
        .error(R.drawable.food_image)
        .into(holder.foodImage)

      holder.btnAdd.setOnClickListener(null)
      holder.btnRemove.setOnClickListener(null)

      if (!isUnavailable) {
        holder.btnAdd.setOnClickListener {
          if (food.quantity < food.stock_qty) {
            food.quantity++
            holder.tvQuantity.text = food.quantity.toString()
            holder.foodPrice.text = String.format(
              Locale.getDefault(), "₱%.2f", food.price * food.quantity
            )
            onQuantityChanged()
            onQuantityUpdated(food.cart_item_id, food.quantity)
          } else {
            android.widget.Toast.makeText(
              holder.itemView.context,
              "Max stock reached",
              android.widget.Toast.LENGTH_SHORT
            ).show()
          }
        }

        holder.btnRemove.setOnClickListener {
          if (food.quantity > 1) {
            food.quantity--
            holder.tvQuantity.text = food.quantity.toString()
            holder.foodPrice.text = String.format(
              Locale.getDefault(), "₱%.2f", food.price * food.quantity
            )
            onQuantityChanged()
            onQuantityUpdated(food.cart_item_id, food.quantity)
          }
        }
      }

      holder.btnDelete.setOnClickListener {
        val idx = holder.adapterPosition
        if (idx != RecyclerView.NO_POSITION) {
          val removed = items[idx]
          items.removeAt(idx)
          notifyItemRemoved(idx)
          onItemRemoved(removed)
          onQuantityChanged()
        }
      }
    }

    override fun getItemCount() = items.size

    fun updateData(newList: List<FoodItem>) {
      this.items = newList.toMutableList()
      notifyDataSetChanged()
    }

    fun syncStock(updatedList: List<FoodItem>) {
      var changed = false
      items.forEach { currentItem ->
        val updated = updatedList.find { it.item_id == currentItem.item_id }
        if (updated != null) {
          currentItem.stock_qty = updated.stock_qty
          currentItem.price = updated.price
          if (currentItem.quantity > currentItem.stock_qty) {
            currentItem.quantity = currentItem.stock_qty.coerceAtLeast(1)
            changed = true
          }
        }
      }
      if (changed) {
        notifyDataSetChanged()
        onQuantityChanged()
      }
    }

    // ✅ Called from fragment to mark unavailable items
    fun markUnavailableItems(itemIds: List<Int>) {
      unavailableItemIds.clear()
      unavailableItemIds.addAll(itemIds)
      notifyDataSetChanged()
    }

    fun hasUnavailableItems(): Boolean = unavailableItemIds.isNotEmpty()

    fun getItems(): List<FoodItem> = items
  }