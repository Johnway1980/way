package com.alphadoer.trader.data.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alphadoer.trader.R

/**
 * 通知工具：创建渠道并按偏好生成通知。
 */
object NotificationHelper {
    private const val CHANNEL_ID = "reminder_channel"
    private const val CHANNEL_NAME = "提醒"
    private const val CHANNEL_DESC = "交易与复盘提醒"

    fun notifyReminder(
        context: Context,
        type: String,
        message: String,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean
    ) {
        createChannel(context, soundEnabled, vibrationEnabled)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("AlphaDoer")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)

        if (!soundEnabled) {
            builder.setSilent(true)
        }
        if (vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 200, 150, 200))
        } else {
            builder.setVibrate(longArrayOf(0L))
        }

        NotificationManagerCompat.from(context)
            .notify(type.hashCode(), builder.build())
    }

    private fun createChannel(
        context: Context,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = mgr.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
            description = CHANNEL_DESC
            enableLights(false)
            enableVibration(vibrationEnabled)
            if (!soundEnabled) {
                setSound(null, null)
            }
        }
        mgr.createNotificationChannel(channel)
    }
}
