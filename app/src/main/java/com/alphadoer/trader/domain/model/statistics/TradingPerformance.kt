package com.alphadoer.trader.domain.model.statistics

import com.squareup.moshi.JsonClass

/**
 * 整体交易绩效
 */
@JsonClass(generateAdapter = true)
data class TradingPerformance(
    val period: String, // 统计周期，如 "2024-01" 或 "all"
    val totalReturn: Double, // 总收益率（%）
    val annualizedReturn: Double?, // 年化收益率（%）
    val totalProfitLoss: Double, // 总盈亏金额
    val sharpeRatio: Double?, // 夏普比率
    val maxDrawdown: Double, // 最大回撤（%）
    val maxDrawdownDuration: Int, // 最大回撤持续时间（天）
    val winRate: Double, // 胜率（%）
    val profitLossRatio: Double, // 盈亏比
    val totalTrades: Int, // 总交易次数
    val averageHoldingDays: Double, // 平均持仓天数
    val volatility: Double?, // 波动率
    val sortinoRatio: Double?, // 索提诺比率
    val calmarRatio: Double?, // 卡玛比率
    val createdAt: Long = System.currentTimeMillis()
)
