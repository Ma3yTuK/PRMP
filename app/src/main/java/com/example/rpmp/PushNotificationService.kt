package com.example.rpmp

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class PushNotificationService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle the received push notification
        if (remoteMessage.getNotification() != null) {
            val title = remoteMessage.getNotification()!!.title
            val body = remoteMessage.getNotification()!!.body

            // Customize notification handling as per your requirements
            sendNotification(title!!, body!!)
        }
    }

    private fun sendNotification(title: String, body: String) {
        // Create an intent to open the desired activity when the notification is tapped
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Build the notification
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.ic_secure)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)

        val channelId = "channel_id"
        val channel = NotificationChannel(
            channelId,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(0, notificationBuilder.build())
    }
}