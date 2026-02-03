package com.alphadoer.trader.domain.model.settings

import com.squareup.moshi.JsonClass

/**
 * 通知偏好
 */
@JsonClass(generateAdapter = true)
data class NotificationPreference(
    val tradeReminder: Boolean = true,      // 交易提醒
    val reviewReminder: Boolean = true,      // 复盘提醒
    val marketAlert: Boolean = false,        // 市场异动提醒
    val mistakeAlert: Boolean = true,        // 错误预警
    val dailySummary: Boolean = true,        // 每日总结
    val soundEnabled: Boolean = true,        // 声音提示
    val vibrationEnabled: Boolean = true,    // 震动反馈
    val quietHoursStart: String? = null,     // 免打扰开始时间 HH:mm
    val quietHoursEnd: String? = null,       // 免打扰结束时间 HH:mm
    val updatedAt: Long = System.currentTimeMillis()
)
