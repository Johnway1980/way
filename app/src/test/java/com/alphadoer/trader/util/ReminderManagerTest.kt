package com.alphadoer.trader.util

import com.alphadoer.trader.data.util.ReminderManager
import com.alphadoer.trader.domain.model.settings.NotificationPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ReminderManagerTest {

    @Test
    fun `trade reminder scheduled next morning when no quiet hours`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 26, 8, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val pref = NotificationPreference(tradeReminder = true, reviewReminder = true)
        val schedule = ReminderManager.scheduleTradeReminder(pref, nowMillis = cal.timeInMillis)!!
        val targetCal = Calendar.getInstance().apply { timeInMillis = schedule.triggerAtMillis }
        assertEquals(Calendar.JANUARY, targetCal.get(Calendar.MONTH))
        assertEquals(26, targetCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, targetCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(20, targetCal.get(Calendar.MINUTE))
    }

    @Test
    fun `trade reminder moves out of quiet hours`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 26, 8, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val pref = NotificationPreference(
            tradeReminder = true,
            reviewReminder = true,
            quietHoursStart = "09:00",
            quietHoursEnd = "10:00"
        )
        val schedule = ReminderManager.scheduleTradeReminder(pref, nowMillis = cal.timeInMillis)!!
        val targetCal = Calendar.getInstance().apply { timeInMillis = schedule.triggerAtMillis }
        // 应移到静默结束 10:00 之后（等于10:00）
        assertEquals(10, targetCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, targetCal.get(Calendar.MINUTE))
        assertTrue(schedule.triggerAtMillis > cal.timeInMillis)
    }
}
