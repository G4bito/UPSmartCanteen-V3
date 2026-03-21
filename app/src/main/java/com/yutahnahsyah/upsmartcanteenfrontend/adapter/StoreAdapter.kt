package com.yutahnahsyah.upsmartcanteenfrontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yutahnahsyah.upsmartcanteenfrontend.Constants
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.Stall

class StoreAdapter(
  private var stalls: List<Stall>,
  private val onClick: (Stall) -> Unit
) : RecyclerView.Adapter<StoreAdapter.StoreViewHolder>() {

  private var fullStallList: List<Stall> = stalls

  class StoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val name: TextView = view.findViewById(R.id.storeName)
    val category: TextView = view.findViewById(R.id.storeCategory)
    val image: ImageView = view.findViewById(R.id.storeImage)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_store, parent, false)
    return StoreViewHolder(view)
  }

  override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
    val stall = stalls[position]
    holder.name.text = stall.stall_name
    holder.category.text = stall.location // Using location as the subtitle/category

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
