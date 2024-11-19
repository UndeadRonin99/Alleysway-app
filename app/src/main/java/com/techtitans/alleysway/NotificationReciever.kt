// File: app/src/main/java/com/example/alleysway/NotificationReceiver.kt
package com.techtitans.alleysway

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * BroadcastReceiver to handle scheduled notifications.
 */
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Retrieve the notification message from the intent extras
        val mealTitle = intent.getStringExtra("MEAL_TITLE")

        // Define the custom sound URI from the raw resources
        val soundResID = R.raw.custom_notification_sound
        val soundUri: Uri = Uri.parse("android.resource://${context.packageName}/${soundResID}")

        // Create an Intent to open HomePage when the notification is clicked
        val notificationIntent = Intent(context, HomePage::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Create a PendingIntent wrapping the notificationIntent
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification with desired properties
        val notificationBuilder = NotificationCompat.Builder(context, "GYM_CHANNEL")
            .setSmallIcon(R.drawable.image_alleysway_logo) // Set the notification icon
            .setContentTitle("Enjoy your workout") // Set the notification title
            .setContentText(mealTitle) // Set the notification text
            .setStyle(NotificationCompat.BigTextStyle().bigText(mealTitle)) // Expandable text style
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Set high priority
            .setSound(soundUri) // Set custom notification sound
            .setContentIntent(pendingIntent) // Set the intent to fire on click
            .setAutoCancel(true) // Dismiss the notification when clicked

        // Get the NotificationManager system service
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // For Android O and above, create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "GYM_CHANNEL",
                "Gym Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for Gym training"
                setSound(soundUri, null) // Set the channel's sound
            }
            notificationManager.createNotificationChannel(channel) // Register the channel
        }

        // Display the notification with a unique ID
        notificationManager.notify(1, notificationBuilder.build())
    }
}
