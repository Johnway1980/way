package com.alphadoer.trader.domain.model.review

import com.squareup.moshi.JsonClass

/**
 * 板块表现数据
 */
@JsonClass(generateAdapter = true)
data class SectorPerformance(
    val sectorId: Long,
    val sectorName: String,
    val changeRate: Double,          // 板块涨跌幅（%）
    val capitalInflow: Double,        // 资金净流入（亿元）
    val limitUpCount: Int,            // 涨停数量
    val totalVolume: Double,          // 总成交量
    val leadingStocks: List<String>,  // 领涨股票代码列表
    val trend: SectorTrend,           // 板块趋势
    val rotationPhase: RotationPhase // 轮动阶段
) {
    /**
     * 板块趋势
     */
    enum class SectorTrend {
        STRONG_RISING,    // 强势上涨
        WEAK_RISING,      // 弱势上涨
        CONSOLIDATION,    // 震荡
        WEAK_DECLINING,   // 弱势下跌
        STRONG_DECLINING  // 强势下跌
    }
    
    /**
     * 轮动阶段
     */
    enum class RotationPhase {
        NEW_START,        // 新启动
        CONTINUOUS_STRONG, // 连续强势
        PEAK,            // 见顶
        DECLINING,       // 退潮
        BOTTOM           // 筑底
    }
}
