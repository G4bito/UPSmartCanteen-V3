package com.yutahnahsyah.upsmartcanteenfrontend.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteenfrontend.R
import com.yutahnahsyah.upsmartcanteenfrontend.adapter.NotificationAdapter
import com.yutahnahsyah.upsmartcanteenfrontend.data.model.Notification

class NotificationsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val rvNotifications = view.findViewById<RecyclerView>(R.id.rvNotifications)
        rvNotifications.layoutManager = LinearLayoutManager(requireContext())

        // Dummy data for notifications
        val notifications = listOf(
            Notification("1", "Order Prepared", "Your order from Stall A is ready for pickup!", "5m ago", false),
            Notification("2", "Promo Alert", "Get 20% off on all drinks today!", "1h ago", false),
            Notification("3", "Payment Successful", "Your payment for Order #12345 was successful.", "Yesterday", true),
            Notification("4", "Welcome!", "Thanks for joining UP Smart Canteen!", "2 days ago", true)
        )

        val adapter = NotificationAdapter(notifications)
        rvNotifications.adapter = adapter
    }
}
