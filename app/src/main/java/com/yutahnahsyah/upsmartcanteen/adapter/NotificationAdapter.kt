package com.yutahnahsyah.upsmartcanteen.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteen.R
import com.yutahnahsyah.upsmartcanteen.data.model.Notification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class NotificationAdapter(private var notifications: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvNotificationTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvNotificationMessage)
        val tvTime: TextView = view.findViewById(R.id.tvNotificationTime)
        val readStatusIndicator: View = view.findViewById(R.id.readStatusIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.tvTitle.text = notification.title
        holder.tvMessage.text = notification.message
        holder.tvTime.text = formatTime(notification.created_at)
        holder.readStatusIndicator.visibility = if (notification.is_read) View.GONE else View.VISIBLE
    }

    private fun formatTime(raw: String?): String {
        if (raw == null) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(raw) ?: return raw
            val now = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time
            val diff = now.time - date.time
            val minutes = diff / 60000
            val hours = minutes / 60
            val days = hours / 24
            when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "${minutes}m ago"
                hours < 24 -> "${hours}h ago"
                days == 1L -> "Yesterday"
                else -> "${days} days ago"
            }
        } catch (e: Exception) {
            raw
        }
    }

    fun updateData(newNotifications: List<Notification>) {
        this.notifications = newNotifications
        notifyDataSetChanged()
    }

    override fun getItemCount() = notifications.size
}