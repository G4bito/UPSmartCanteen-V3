package com.yutahnahsyah.upsmartcanteenfrontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.Notification

class NotificationAdapter(private val notifications: List<Notification>) :
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
        holder.tvTime.text = notification.time
        
        holder.readStatusIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE
    }

    override fun getItemCount() = notifications.size
}
