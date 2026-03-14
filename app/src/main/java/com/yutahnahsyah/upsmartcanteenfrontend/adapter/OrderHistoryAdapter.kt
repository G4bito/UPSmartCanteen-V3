package com.yutahnahsyah.upsmartcanteenfrontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.Order

class OrderHistoryAdapter(private val orders: List<Order>) :
    RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderId: TextView = view.findViewById(R.id.tvOrderId)
        val tvOrderDate: TextView = view.findViewById(R.id.tvOrderDate)
        val tvOrderStatus: TextView = view.findViewById(R.id.tvOrderStatus)
        val tvOrderItems: TextView = view.findViewById(R.id.tvOrderItems)
        val tvOrderTotal: TextView = view.findViewById(R.id.tvOrderTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.tvOrderId.text = "Order #${order.orderId}"
        holder.tvOrderDate.text = order.date
        holder.tvOrderStatus.text = order.status
        holder.tvOrderItems.text = order.items
        holder.tvOrderTotal.text = "₱${String.format("%.2f", order.totalPrice)}"
    }

    override fun getItemCount() = orders.size
}
