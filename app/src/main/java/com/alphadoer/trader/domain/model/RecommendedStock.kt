package com.alphadoer.trader.domain.model

import com.squareup.moshi.JsonClass

/**
 * 推荐股票
 */
@JsonClass(generateAdapter = true)
data class RecommendedStock(
    val stockCode: String,
    val stockName: String,
    val market: String, // "SH" | "SZ" | "BJ"
    val recommendation: RecommendationType,
    val reason: String,
    val confidence: Double, // 0.0-1.0
    val targetPrice: Double? = null,
    val riskLevel: RiskLevel = RiskLevel.MEDIUM,
    val sectorName: String? = null // 所属板块名称（多个板块时标明）
) {
    enum class RecommendationType {
        BUY,        // 买入
        SELL,       // 卖出
        HOLD,       // 持有
        WATCH       // 关注
    }
    
    enum class RiskLevel {
        LOW,        // 低风险
        MEDIUM,     // 中等风险
        HIGH        // 高风险
    }
}
