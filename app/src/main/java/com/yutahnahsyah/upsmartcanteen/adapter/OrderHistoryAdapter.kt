package com.yutahnahsyah.upsmartcanteen.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteen.data.model.Order
import com.yutahnahsyah.upsmartcanteen.R
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class OrderHistoryAdapter(private var orders: List<Order>) :
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
    val llStatusTimeContainer: LinearLayout = view.findViewById(R.id.llStatusTimeContainer)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.item_order, parent, false)
    return OrderViewHolder(view)
  }

  override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
    val order = orders[position]
    val context = holder.itemView.context

    holder.tvOrderId.text = "# ${order.orderId}"
    holder.tvOrderStatus.text = order.status.uppercase(Locale.ROOT)

    val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    // ✅ Prioritize snapshot data, fall back to current profile if snapshot is missing
    holder.tvCustomerName.text = order.customer_name?.takeIf { it.isNotEmpty() }
      ?: sharedPref.getString("user_full_name", "Customer")

    holder.tvDepartment.text = order.department?.takeIf { it.isNotEmpty() }
      ?: sharedPref.getString("user_department", "Canteen")

    holder.tvOrderPlacedTime.text = "Order Placed: ${formatDateTime(order.date)}"

    when (order.status.lowercase()) {
      "completed", "picked_up" -> {
        setStatusStyle(holder, context, "#2E7D32", "#E8F5E9", R.drawable.ic_check_circle)
        holder.tvOrderTimeStatus.text = "Completed: ${formatDateTime(order.date)}"
      }

      "cancelled" -> {
        setStatusStyle(holder, context, "#D32F2F", "#FFEBEE", R.drawable.ic_cancel_circle)
        holder.tvOrderTimeStatus.text = "Cancelled: ${formatDateTime(order.date)}"
      }

      else -> {
        setStatusStyle(holder, context, "#F57C00", "#FFF3E0", R.drawable.ic_history)
        holder.tvOrderTimeStatus.text = "Placed: ${formatDateTime(order.date)}"
      }
    }

    holder.tvOrderItems.text = order.items
    holder.tvOrderTotal.text = String.format(Locale.getDefault(), "₱%.2f", order.totalPrice)
  }

  private fun formatDateTime(raw: String?): String {
    if (raw.isNullOrEmpty()) return "—"
    return try {
      val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
      inputFormat.timeZone = TimeZone.getTimeZone("UTC")
      val outputFormat = SimpleDateFormat("M/d/yyyy, h:mm:ss a", Locale.getDefault())
      outputFormat.timeZone = TimeZone.getTimeZone("Asia/Manila")
      val date = inputFormat.parse(raw)
      outputFormat.format(date!!)
    } catch (e: Exception) {
      raw
    }
  }

  private fun setStatusStyle(
    holder: OrderViewHolder,
    context: Context,
    textColor: String,
    bgColor: String,
    iconRes: Int
  ) {
    holder.tvOrderStatus.setTextColor(Color.parseColor(textColor))
    holder.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor(bgColor))
    holder.tvOrderTimeStatus.setTextColor(Color.parseColor(textColor))
    holder.ivStatusIcon.setImageResource(iconRes)
    holder.ivStatusIcon.imageTintList = ColorStateList.valueOf(Color.parseColor(textColor))
  }

  override fun getItemCount() = orders.size

  fun updateData(newOrders: List<Order>) {
    this.orders = newOrders
    notifyDataSetChanged()
  }
}