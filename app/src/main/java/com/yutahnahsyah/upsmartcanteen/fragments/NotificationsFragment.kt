package com.yutahnahsyah.upsmartcanteen.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yutahnahsyah.upsmartcanteen.R
import com.yutahnahsyah.upsmartcanteen.RetrofitClient
import com.yutahnahsyah.upsmartcanteen.adapter.NotificationAdapter
import com.yutahnahsyah.upsmartcanteen.data.model.Notification
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

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
        val session = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = session.getString("token", null)

        if (token == null) {
            loadFromLocal()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getNotifications("Bearer $token")
                if (response.isSuccessful) {
                    val notifications = response.body() ?: emptyList()
                    adapter.updateData(notifications)
                    saveNotificationsLocally(notifications)
                } else {
                    loadFromLocal()
                }
            } catch (e: Exception) {
                loadFromLocal()
            }
        }
    }

    private fun loadFromLocal() {
        val session = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val employeeId = session.getString("employee_id", null) ?: return
        val prefs = requireContext().getSharedPreferences("notifications_$employeeId", Context.MODE_PRIVATE)
        val json = prefs.getString("notif_list", "[]")
        val array = JSONArray(json)
        val notifications = mutableListOf<Notification>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            notifications.add(
                Notification(
                    id = obj.getInt("id"),   // ✅ changed from getString to getInt
                    title = obj.getString("title"),
                    message = obj.getString("message"),
                    is_read = obj.getBoolean("is_read"),
                    created_at = obj.getString("created_at")
                )
            )
        }
        adapter.updateData(notifications)
    }

    private fun saveNotificationsLocally(notifications: List<Notification>) {
        val session = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val employeeId = session.getString("employee_id", null) ?: return
        val prefs = requireContext().getSharedPreferences("notifications_$employeeId", Context.MODE_PRIVATE)
        val array = JSONArray()
        notifications.forEach { notif ->
            val obj = JSONObject()
            obj.put("id", notif.id)
            obj.put("title", notif.title)
            obj.put("message", notif.message)
            obj.put("is_read", notif.is_read)
            obj.put("created_at", notif.created_at)
            array.put(obj)
        }
        prefs.edit().putString("notif_list", array.toString()).apply()
    }
}