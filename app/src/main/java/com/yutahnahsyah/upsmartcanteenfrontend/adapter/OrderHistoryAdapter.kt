package com.yutahnahsyah.upsmartcanteenfrontend.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteenfrontend.OrderResponse
import com.yutahnahsyah.upsmartcanteenfrontend.R
import java.util.Locale

class OrderHistoryAdapter(private var orders: List<OrderResponse>) :
    RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderId: TextView = view.findViewById(R.id.tvOrderId)
        val tvOrderStatus: TextView = view.findViewById(R.id.tvOrderStatus)
        val tvDepartment: TextView = view.findViewById(R.id.tvDepartment)
        val tvCustomerName: TextView = view.findViewById(R.id.tvCustomerName)
        val tvOrderItems: TextView = view.findViewById(R.id.tvOrderItems)
        val tvOrderTotal: TextView = view.findViewById(R.id.tvOrderTotal)
        val tvOrderPlacedTime: TextView = view.findViewById(R.id.tvOrderPlacedTime)
        val tvOrderTimeStatus: TextView = view.findViewById(R.id.tvOrderTimeStatus)
        val ivStatusIcon: ImageView = view.findViewById(R.id.ivStatusIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context
        
        holder.tvOrderId.text = "# ${order.order_id}"
        holder.tvOrderStatus.text = order.status.uppercase()
        
        // Since the backend might not return these yet, we use placeholders or user info
        val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        holder.tvCustomerName.text = sharedPref.getString("user_full_name", "Customer")
        holder.tvDepartment.text = sharedPref.getString("user_department", "Canteen")
        
        // Assuming order_date is the placed time
        holder.tvOrderPlacedTime.text = "Order Placed: ${order.order_date}"
        
        // Logic for Status Colors and Icons
        when (order.status.lowercase()) {
            "completed", "picked_up" -> {
                setStatusStyle(holder, context, "#2E7D32", "#E8F5E9", R.drawable.ic_check_circle)
                holder.tvOrderTimeStatus.text = "Completed: ${order.order_date}"
            }
            "cancelled" -> {
                setStatusStyle(holder, context, "#D32F2F", "#FFEBEE", R.drawable.ic_cancel_circle)
                holder.tvOrderTimeStatus.text = "Cancelled: ${order.order_date}"
            }
            else -> { // Pending or Processing
                setStatusStyle(holder, context, "#F57C00", "#FFF3E0", R.drawable.ic_history)
                holder.tvOrderTimeStatus.text = "Status: ${order.status}"
            }
        }

        // Display items (assuming we format them as a string for now)
        holder.tvOrderItems.text = "Order Summary" 
        holder.tvOrderTotal.text = String.format(Locale.getDefault(), "₱%.2f", order.total_price)
    }

    private fun setStatusStyle(holder: OrderViewHolder, context: Context, textColor: String, bgColor: String, iconRes: Int) {
        holder.tvOrderStatus.setTextColor(Color.parseColor(textColor))
        holder.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor(bgColor))
        holder.tvOrderTimeStatus.setTextColor(Color.parseColor(textColor))
        holder.ivStatusIcon.setImageResource(iconRes)
        holder.ivStatusIcon.imageTintList = ColorStateList.valueOf(Color.parseColor(textColor))
    }

    override fun getItemCount() = orders.size

    fun updateData(newOrders: List<OrderResponse>) {
        this.orders = newOrders
        notifyDataSetChanged()
    }
}
