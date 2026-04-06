package com.happyclaw.hikinghappy.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.happyclaw.hikinghappy.MainActivity
import com.happyclaw.hikinghappy.R

object RecordingNotification {

    const val CHANNEL_ID = "channel_recording"
    const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "HikingHappy Recording",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows recording status with altitude, speed, and duration"
            setShowBadge(false)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun build(
        context: Context,
        altitude: Double,
        speed: Double,
        durationSec: Long
    ): Notification {
        val formattedAlt = if (altitude.isNaN()) "---" else String.format("%,.0f m", altitude)
        val formattedSpd = String.format("%.1f km/h", speed * 3.6)
        val hours = durationSec / 3600
        val minutes = (durationSec % 3600) / 60
        val seconds = durationSec % 60
        val formattedDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        val contentText = "$formattedAlt | $formattedSpd | $formattedDuration"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("HikingHappy - Recording")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
