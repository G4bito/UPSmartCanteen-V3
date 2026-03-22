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

class StallAdapter(
    private val stalls: List<Stall>,
    private val onStallClick: (Stall) -> Unit
) : RecyclerView.Adapter<StallAdapter.StallViewHolder>() {

    class StallViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivStallImage: ImageView = view.findViewById(R.id.ivStallImage)
        val tvStallName: TextView = view.findViewById(R.id.tvStallName)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StallViewHolder {
        // Inflates the layout for a single stall row
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stall, parent, false)
        return StallViewHolder(view)
    }

  override fun onBindViewHolder(holder: StallViewHolder, position: Int) {
    val stall = stalls[position]

    holder.tvStallName.text = stall.stall_name
    holder.tvLocation.text = stall.location

    // is_active is already filtered by the API — only active stalls show here
    // is_open controls whether the stall is accepting orders
    if (stall.is_open) {
      holder.tvStatus.text = "Open"
      holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
    } else {
      holder.tvStatus.text = "Closed"
      holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
    }

    val imageUrl = Constants.getFullImageUrl(stall.stall_image_url)
    Glide.with(holder.itemView.context)
      .load(imageUrl)
      .placeholder(R.drawable.food_image)
      .error(R.drawable.food_image)
      .centerCrop()
      .into(holder.ivStallImage)

    holder.itemView.setOnClickListener {
      onStallClick(stall)
    }
  }

    override fun getItemCount(): Int = stalls.size
}
