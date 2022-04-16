package com.speed.car.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ScheduleNotificationService : Service() {

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		intent?.let {
//			val objectNoti = intent.getStringExtra(TITLE)
//			val notify = PingNotification.newInstance(this, id, title ?: "", message ?: "")
//				.showNotification()
//			startForeground(10, notify)
		}
		return START_STICKY
	}

	override fun onBind(intent: Intent?): IBinder? {
		return null
	}
}
