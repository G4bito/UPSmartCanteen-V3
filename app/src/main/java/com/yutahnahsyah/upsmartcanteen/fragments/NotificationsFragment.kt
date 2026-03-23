package com.yutahnahsyah.upsmartcanteen.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteen.R
import com.yutahnahsyah.upsmartcanteen.adapter.NotificationAdapter
import com.yutahnahsyah.upsmartcanteen.data.model.Notification
import org.json.JSONArray

class NotificationsFragment : Fragment() {

    private lateinit var adapter: NotificationAdapter
    private lateinit var rvNotifications: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvNotifications = view.findViewById(R.id.rvNotifications)

        view.findViewById<View>(R.id.toolbar).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotificationAdapter(emptyList())
        rvNotifications.adapter = adapter

        loadNotifications()
    }

    private fun loadNotifications() {
        // ✅ Get the logged-in user's employee_id
        val session = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val employeeId = session.getString("employee_id", null)

        // ✅ If no user logged in, show empty list
        if (employeeId.isNullOrEmpty()) {
            adapter.updateData(emptyList())
            return
        }

        // ✅ Use user-specific storage — matches what MyFirebaseMessagingService saves
        val prefs = requireContext().getSharedPreferences("notifications_$employeeId", Context.MODE_PRIVATE)
        val json = prefs.getString("notif_list", "[]")
        val array = JSONArray(json)

        val notifications = mutableListOf<Notification>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            notifications.add(
                Notification(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    message = obj.getString("message"),
                    is_read = obj.getBoolean("is_read"),
                    created_at = obj.getString("created_at")
                )
            )
        }

        adapter.updateData(notifications)
    }
}