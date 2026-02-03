package com.alphadoer.trader.data.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.alphadoer.trader.domain.model.settings.NotificationPreference

/**
 * 将 ReminderManager 计算的时间点调度到系统 AlarmManager。
 * 不改变原有架构，仅新增调度层；具体通知展示由 ReminderReceiver 处理。
 */
object ReminderScheduler {
    private const val ACTION_REMINDER = "com.alphadoer.trader.action.REMINDER"
    private const val EXTRA_TYPE = "extra_type"
    private const val EXTRA_MESSAGE = "extra_message"
    private const val EXTRA_SOUND = "extra_sound"
    private const val EXTRA_VIBRATION = "extra_vibration"

    fun scheduleAll(context: Context, preference: NotificationPreference) {
        scheduleTrade(context, preference)
        scheduleReview(context, preference)
    }

    fun scheduleTrade(context: Context, preference: NotificationPreference) {
        val schedule = ReminderManager.scheduleTradeReminder(preference) ?: return
        setAlarm(context, schedule, preference)
    }

    fun scheduleReview(context: Context, preference: NotificationPreference) {
        val schedule = ReminderManager.scheduleReviewReminder(preference) ?: return
        setAlarm(context, schedule, preference)
    }

    private fun setAlarm(
        context: Context,
        schedule: ReminderManager.ReminderSchedule,
        preference: NotificationPreference
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pi = PendingIntent.getBroadcast(
            context,
            schedule.type.hashCode(),
            Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_REMINDER
                putExtra(EXTRA_TYPE, schedule.type.name)
                putExtra(EXTRA_MESSAGE, schedule.message)
                putExtra(EXTRA_SOUND, preference.soundEnabled)
                putExtra(EXTRA_VIBRATION, preference.vibrationEnabled)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = schedule.triggerAtMillis

        // Android 12+ 需要精确闹钟权限；无权限时降级为非精确以避免崩溃
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.w("ReminderScheduler", "Exact alarms not allowed, fallback to setAndAllowWhileIdle")
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }
}
