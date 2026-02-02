package com.alphadoer.trader.domain.model.settings

import com.squareup.moshi.JsonClass

/**
 * 交易偏好
 */
@JsonClass(generateAdapter = true)
data class TradingPreference(
    val tradingStyle: TradingStyle,
    val riskLevel: RiskLevel,
    val positionStrategy: PositionStrategy,
    val defaultStopLoss: Double?, // 默认止损比例（%）
    val defaultTakeProfit: Double?, // 默认止盈比例（%）
    val maxPositionCount: Int?, // 最大持仓数量
    val updatedAt: Long = System.currentTimeMillis()
) {
    enum class TradingStyle {
        SHORT_TERM,  // 短线交易
        SWING,       // 波段交易
        LONG_TERM    // 长线投资
    }
    
    enum class RiskLevel {
        CONSERVATIVE, // 保守
        MODERATE,     // 稳健
        AGGRESSIVE    // 激进
    }
    
    enum class PositionStrategy {
        EQUAL_WEIGHT,      // 等权重
        CONCENTRATED,      // 集中持仓
        DIVERSIFIED        // 分散持仓
    }
}
