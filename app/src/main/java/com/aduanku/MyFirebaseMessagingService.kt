package com.aduanku

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.aduanku.CuacaActivity  // Replace with the activity you want to open when the notification is clicked

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Called when a message is received
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if the message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            // Handle the data payload
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            // Show notification with the data payload
            sendNotification(title, body)
        }

        // Check if the message contains a notification payload (notification displayed in the system tray)
        remoteMessage.notification?.let {
            // Show notification with the notification payload
            sendNotification(it.title, it.body)
        }
    }

    // Called when a new FCM token is generated (e.g., after app installation or reinstallation)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send the new token to your server (Firestore or backend)
        // Example: saveTokenToFirestore(token)
    }

    // Function to show the notification
    private fun sendNotification(title: String?, body: String?) {
        val intent = Intent(this, CuacaActivity::class.java)
        // Create a PendingIntent to open the app when the notification is clicked
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create the notification
        val notificationBuilder = NotificationCompat.Builder(this, "FCM_CHANNEL")
            .setSmallIcon(R.drawable.ic_notifications)  // Use your own icon
            .setContentTitle(title ?: "No title")
            .setContentText(body ?: "No message")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)  // Automatically remove the notification when clicked

        // Get the NotificationManager system service
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Notify the user with notification ID (1 in this case)
        notificationManager.notify(1, notificationBuilder.build())
    }
}
