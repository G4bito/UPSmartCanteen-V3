package com.yutahnahsyah.upsmartcanteen.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.yutahnahsyah.upsmartcanteen.Constants
import com.yutahnahsyah.upsmartcanteen.R
import com.yutahnahsyah.upsmartcanteen.data.model.StallCart
import java.util.Locale

class StallCartAdapter(
    private var stallCarts: List<StallCart>,
    private val onViewCartClick: (StallCart) -> Unit
) : RecyclerView.Adapter<StallCartAdapter.StallCartViewHolder>() {

    class StallCartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stallName: TextView = view.findViewById(R.id.name)
        val itemCount: TextView = view.findViewById(R.id.itemCount)
        val subtotal: TextView = view.findViewById(R.id.price)
        val itemsContainer: LinearLayout = view.findViewById(R.id.cartItems)
        val btnViewCart: MaterialButton = view.findViewById(R.id.btnViewCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StallCartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return StallCartViewHolder(view)
    }

    override fun onBindViewHolder(holder: StallCartViewHolder, position: Int) {
        val stallCart = stallCarts[position]
        holder.stallName.text = stallCart.stallName
        holder.itemCount.text = "${stallCart.items.size} items"
        holder.subtotal.text = String.format(Locale.getDefault(), "₱%.2f", stallCart.subtotal)

        // Clear and add item images
        holder.itemsContainer.removeAllViews()
        val inflater = LayoutInflater.from(holder.itemView.context)
        
        stallCart.items.take(3).forEach { item ->
            val imageView = inflater.inflate(R.layout.view_cart_item_image, holder.itemsContainer, false) as ShapeableImageView
            
            val imageUrl = Constants.getFullImageUrl(item.image_url)

            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.food_image)
                .into(imageView)
            
            holder.itemsContainer.addView(imageView)
        }

        // Add the "+" button if there are more items or just as a placeholder
        val plusButton = inflater.inflate(R.layout.view_cart_plus_button, holder.itemsContainer, false)
        holder.itemsContainer.addView(plusButton)

        holder.btnViewCart.setOnClickListener { onViewCartClick(stallCart) }
        holder.btnViewCart.text = "View your cart"
    }

    override fun getItemCount() = stallCarts.size

    fun getStallCartAt(position: Int): StallCart {
        return stallCarts[position]
    }

    fun updateData(newList: List<StallCart>) {
        this.stallCarts = newList
        notifyDataSetChanged()
    }
}
