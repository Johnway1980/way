package com.alphadoer.trader.domain.model.settings

import com.squareup.moshi.JsonClass

/**
 * 风险控制配置
 */
@JsonClass(generateAdapter = true)
data class RiskControlConfig(
    val maxSingleLoss: Double?, // 单笔最大亏损（元或%）
    val maxDailyLoss: Double?, // 每日最大亏损限额
    val maxPositionCount: Int?, // 最大持仓数量
    val warningThreshold: Double = 0.8, // 风险预警阈值（达到限额的80%时预警）
    val autoStopLoss: Boolean = false, // 自动止损开关
    val updatedAt: Long = System.currentTimeMillis()
)
