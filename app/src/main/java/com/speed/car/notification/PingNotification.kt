package com.speed.car.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.speed.car.R

class PingNotification(
    val id: Int,
    val context: Context,
    val title: String,
    private val message: String?
) {
    private var notification: NotificationCompat.Builder

    companion object {
        private const val CHANNEL_ID = "channel_id"
        private const val CHANNEL_NAME = "ping_note_notification"
        private const val CHANNEL_DESCRIPTION = "ping_note_to_status_bar"
        fun newInstance(
            context: Context,
            id: Int,
            title: String,
            message: String? = null,
        ): PingNotification {
            return PingNotification(id, context, title, message)
        }
    }

    init {
        createNotificationChannel()
        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_main)
            .setDestination(R.id.mainFragment)
            .setArguments(bundleOf("noteId" to id))
            .createPendingIntent()

        notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(): Notification {
        val build = notification.build()
        with(NotificationManagerCompat.from(context)) {
            notify(id, build)
        }
        return build
    }

    fun cancel(id: Int) {
        with(NotificationManagerCompat.from(context)) {
            cancel(id)
        }
    }
}
