package com.alphadoer.trader.domain.model.statistics

import com.squareup.moshi.JsonClass

/**
 * 分阶段绩效
 */
@JsonClass(generateAdapter = true)
data class PeriodPerformance(
    val periodType: PeriodType,
    val period: String, // 如 "2024-01", "2024-Q1", "2024"
    val performance: TradingPerformance,
    val benchmarkReturn: Double?, // 基准收益率（如沪深300）
    val alpha: Double?, // 超额收益（Alpha）
    val beta: Double? // 市场敏感度（Beta）
) {
    enum class PeriodType {
        MONTHLY,    // 月度
        QUARTERLY,  // 季度
        YEARLY,     // 年度
        BULL_MARKET, // 牛市
        BEAR_MARKET  // 熊市
    }
}
