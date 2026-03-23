package com.yutahnahsyah.upsmartcanteen

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MyFirebaseMessagingService : FirebaseMessagingService() {

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)

    val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Order Update"
    val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Your order is ready!"

    saveNotification(title, body)
    showNotification(title, body)
  }

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    val prefs = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("fcm_token", token).apply()
  }

  private fun saveNotification(title: String, body: String) {
    val userPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val employeeId = userPrefs.getString("employee_id", null)

    // If no employee_id, don't save
    if (employeeId.isNullOrEmpty()) return

    val prefs = getSharedPreferences("notifications_$employeeId", Context.MODE_PRIVATE)
    val existing = prefs.getString("notif_list", "[]")
    val array = JSONArray(existing)

    // Format timestamp in UTC
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")

    val newNotif = JSONObject().apply {
      put("id", System.currentTimeMillis().toString())
      put("title", title)
      put("message", body)
      put("is_read", false)
      put("created_at", sdf.format(Date()))
    }

    val newArray = JSONArray()
    newArray.put(newNotif)
    for (i in 0 until array.length()) {
      if (newArray.length() < 10) {
        newArray.put(array.getJSONObject(i))
      }
    }

    prefs.edit().putString("notif_list", newArray.toString()).apply()
  }

  private fun showNotification(title: String, body: String) {
    val channelId = "order_updates"
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        channelId,
        "Order Updates",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Notifications for order status updates"
        enableVibration(true)
      }
      notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      this, 0, intent,
      PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(this, channelId)
      .setSmallIcon(R.drawable.ic_info)
      .setContentTitle(title)
      .setContentText(body)
      .setAutoCancel(true)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setContentIntent(pendingIntent)
      .build()

    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
  }
}