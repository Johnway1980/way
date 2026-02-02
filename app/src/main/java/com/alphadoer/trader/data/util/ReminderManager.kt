package com.alphadoer.trader.data.util

import com.alphadoer.trader.domain.model.settings.NotificationPreference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 轻量提醒调度器：仅计算下一次触发时间，交由上层决定如何展示/通知。
 */
object ReminderManager {
    enum class ReminderType { TRADE, REVIEW }

    data class ReminderSchedule(
        val type: ReminderType,
        val triggerAtMillis: Long,
        val message: String
    )

    fun scheduleTradeReminder(
        preference: NotificationPreference,
        nowMillis: Long = System.currentTimeMillis()
    ): ReminderSchedule? {
        if (!preference.tradeReminder) return null
        val target = nextTimeTodayOrTomorrow(hour = 9, minute = 20, nowMillis = nowMillis)
        val adjusted = adjustQuietHours(target, preference.quietHoursStart, preference.quietHoursEnd, nowMillis)
        return ReminderSchedule(
            type = ReminderType.TRADE,
            triggerAtMillis = adjusted,
            message = "开盘前检查交易计划"
        )
    }

    fun scheduleReviewReminder(
        preference: NotificationPreference,
        nowMillis: Long = System.currentTimeMillis()
    ): ReminderSchedule? {
        if (!preference.reviewReminder) return null
        val target = nextTimeTodayOrTomorrow(hour = 15, minute = 30, nowMillis = nowMillis)
        val adjusted = adjustQuietHours(target, preference.quietHoursStart, preference.quietHoursEnd, nowMillis)
        return ReminderSchedule(
            type = ReminderType.REVIEW,
            triggerAtMillis = adjusted,
            message = "收盘后复盘与记录"
        )
    }

    private fun nextTimeTodayOrTomorrow(hour: Int, minute: Int, nowMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = nowMillis
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        if (cal.timeInMillis <= nowMillis) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return cal.timeInMillis
    }

    private fun adjustQuietHours(
        targetMillis: Long,
        quietStart: String?,
        quietEnd: String?,
        nowMillis: Long
    ): Long {
        if (quietStart.isNullOrBlank() || quietEnd.isNullOrBlank()) return targetMillis
        val df = SimpleDateFormat("HH:mm", Locale.getDefault())
        return try {
            val calTarget = Calendar.getInstance().apply { timeInMillis = targetMillis }
            val calStart = Calendar.getInstance().apply { timeInMillis = targetMillis }
            val start = df.parse(quietStart)
            val end = df.parse(quietEnd)
            if (start != null && end != null) {
                calStart.set(Calendar.HOUR_OF_DAY, start.hours)
                calStart.set(Calendar.MINUTE, start.minutes)
                val calEnd = Calendar.getInstance().apply {
                    timeInMillis = targetMillis
                    set(Calendar.HOUR_OF_DAY, end.hours)
                    set(Calendar.MINUTE, end.minutes)
                }
                val inQuiet = calTarget.timeInMillis in calStart.timeInMillis..calEnd.timeInMillis
                return if (inQuiet) {
                    // 移到静默结束时间，若仍过去则顺延一天
                    val adjusted = calEnd.timeInMillis
                    if (adjusted <= nowMillis) adjusted + 24 * 60 * 60 * 1000 else adjusted
                } else targetMillis
            } else {
                targetMillis
            }
        } catch (_: Exception) {
            targetMillis
        }
    }
}
