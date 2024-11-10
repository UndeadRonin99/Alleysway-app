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

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mealTitle = intent.getStringExtra("MEAL_TITLE")

        val soundResID = R.raw.custom_notification_sound
        // Get custom sound URI from res/raw directory
        val soundUri: Uri = Uri.parse("android.resource://${context.packageName}/${soundResID}")

        // Create an Intent to open HomePage.kt when the notification is clicked
        val notificationIntent = Intent(context, HomePage::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Create a PendingIntent with the notificationIntent
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, "GYM_CHANNEL")
            .setSmallIcon(R.drawable.image_alleysway_logo)
            .setContentTitle("Enjoy your workout")
            .setContentText(mealTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mealTitle))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setContentIntent(pendingIntent) // Set the content intent here
            .setAutoCancel(true) // Automatically remove the notification when clicked

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
                setSound(soundUri, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(1, notificationBuilder.build())
    }
}
