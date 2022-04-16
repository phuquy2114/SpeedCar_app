package com.speed.car.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.speed.car.R

interface NotificationRepository {
    fun sendNotification(
        channelDetail: ChannelDetail,
        notificationContent: NotificationContent,
    )
}

class NotificationRepositoryImpl(
    private val mContext: Context
) : NotificationRepository {
    private fun getManager(): NotificationManager {
        return ContextCompat.getSystemService(
            mContext,
            NotificationManager::class.java
        ) as NotificationManager
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun sendNotification(
        channelDetail: ChannelDetail,
        notificationContent: NotificationContent,
    ) {
        val notificationManager = getManager()
        val channel = channelDetail.channel
        if (notificationManager.getNotificationChannel(channel.id) == null) {
            val mChannel =
                NotificationChannel(channel.id, channel.toChannelName(), channelDetail.importance)
            mChannel.apply {
                description = channel.toDescription()
            }
            notificationManager.createNotificationChannel(mChannel)
        }
        val notification = NotificationCompat
            .Builder(mContext, channel.id)
            .setSmallIcon(R.drawable.ic_baseline_notifications)
            .setContentTitle(notificationContent.title)
            .setContentText(notificationContent.text)
            .build()
        notificationManager.notify(notificationContent.id, notification)
    }
}

data class ChannelDetail(
    val channel: NotificationChannelType,
    val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
)

data class NotificationContent(val id: Int, val title: String, val text: String)
enum class NotificationChannelType(val id: String) {
    SPEED_CAR("speedcar-notification-channel-id");

    fun toChannelName(): String {
        return when (this) {
            SPEED_CAR -> "SpeedCarNotification"
        }
    }

    fun toDescription(): String {
        return when (this) {
            SPEED_CAR -> ""
        }
    }
}