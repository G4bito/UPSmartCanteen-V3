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

class MyFirebaseMessagingService : FirebaseMessagingService() {

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)

    val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Order Update"
    val body =
      remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Your order is ready!"

    showNotification(title, body)
  }

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    // Save token to your backend when it refreshes
    val prefs = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("fcm_token", token).apply()
  }

  private fun showNotification(title: String, body: String) {
    val channelId = "order_updates"
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create channel (required for Android 8+)
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

    // Tap notification → open app
    val intent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      this, 0, intent,
      PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(this, channelId)
      .setSmallIcon(R.drawable.ic_info) // replace with your app icon
      .setContentTitle(title)
      .setContentText(body)
      .setAutoCancel(true)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setContentIntent(pendingIntent)
      .build()

    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
  }
}