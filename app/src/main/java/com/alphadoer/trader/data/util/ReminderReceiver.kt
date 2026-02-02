package com.alphadoer.trader.data.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * AlarmManager 到通知的桥接。
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val type = intent.getStringExtra("extra_type") ?: return
        val message = intent.getStringExtra("extra_message") ?: return
        val sound = intent.getBooleanExtra("extra_sound", true)
        val vibration = intent.getBooleanExtra("extra_vibration", true)
        NotificationHelper.notifyReminder(context, type, message, sound, vibration)
    }
}
