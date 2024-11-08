package com.techtitans.alleysway

import android.app.NotificationChannel
import android.app.NotificationManager
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

        val notificationBuilder = NotificationCompat.Builder(context, "GYM_CHANNEL")
            .setSmallIcon(R.drawable.image_alleysway_logo)
            .setContentTitle("Enjoy your workout")
            .setContentText("$mealTitle!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri) // Set the custom sound URI here

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "GYM_CHANNEL",
                "Gym Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for Gym training"
                setSound(soundUri, null) // Set the custom sound for Android Oreo and above
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(1, notificationBuilder.build())
    }
}
