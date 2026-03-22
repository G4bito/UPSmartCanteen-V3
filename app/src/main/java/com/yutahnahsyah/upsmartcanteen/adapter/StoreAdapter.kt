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
import com.yutahnahsyah.upsmartcanteen.data.model.Stall

class StoreAdapter(
  private var stalls: List<Stall>,
  private val onClick: (Stall) -> Unit
) : RecyclerView.Adapter<StoreAdapter.StoreViewHolder>() {

  private var fullStallList: List<Stall> = stalls

  class StoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val name: TextView = view.findViewById(R.id.storeName)
    val category: TextView = view.findViewById(R.id.storeCategory)
    val image: ImageView = view.findViewById(R.id.storeImage)
    val statusDot: View = view.findViewById(R.id.statusDot)           // ← ADD
    val statusText: TextView = view.findViewById(R.id.statusText)     // ← ADD
    val closedOverlay: View = view.findViewById(R.id.closedOverlay)   // ← ADD
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_store, parent, false)
    return StoreViewHolder(view)
  }

  override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
    val stall = stalls[position]
    holder.name.text = stall.stall_name
    holder.category.text = stall.location

    if (stall.is_open) {
      holder.statusText.text = "OPEN"
      holder.statusText.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
      holder.statusDot.setBackgroundResource(R.drawable.bg_status_dot_open)
      holder.closedOverlay.visibility = View.GONE
    } else {
      holder.statusText.text = "CLOSED"
      holder.statusText.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
      holder.statusDot.setBackgroundColor(
        holder.itemView.context.getColor(android.R.color.holo_red_dark)
      )
      holder.closedOverlay.visibility = View.VISIBLE  // dims the image
    }

    val imageUrl = Constants.getFullImageUrl(stall.stall_image_url)
    Glide.with(holder.itemView.context)
      .load(imageUrl)
      .placeholder(R.drawable.store_image)
      .error(R.drawable.store_image)
      .into(holder.image)

    holder.itemView.setOnClickListener { onClick(stall) }
  }

  override fun getItemCount() = stalls.size

  fun updateData(newStalls: List<Stall>) {
    this.stalls = newStalls
    this.fullStallList = newStalls
    notifyDataSetChanged()
  }

  fun filter(query: String) {
    stalls = if (query.isEmpty()) {
      fullStallList
    } else {
      fullStallList.filter {
        it.stall_name.contains(query, ignoreCase = true) ||
                it.location.contains(query, ignoreCase = true)
      }
    }
    notifyDataSetChanged()
  }
}
