package com.hackthecrisis.scrubby

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class WashNotification {

    companion object {
        const val TAG = "WashNotification"
        const val CHANNEL_NAME = "WashChannel"
        const val CHANNEL_ID = "WashChannelId"
        const val NOTIFICATION_ID = 1
    }

    /**
     * Creates and triggers a wash reminder notification
     */
    fun createNotification(context: Context) {
        Log.d(TAG, "Creating wash reminder notification")
        createNotificationChannel(context)
        val intent = Intent(context, WashActivity::class.java).setFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
        )
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val washNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.twotone_pan_tool_24)
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentTitle("Scrubby")
            .setContentText("Friendly reminder to wash your hands")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, washNotification)
    }

    /**
     * Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is
     * new and not in the support library
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val descriptionText = "Notification channel for wash reminder"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}