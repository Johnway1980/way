package com.alphadoer.trader.domain.model.review

import com.squareup.moshi.JsonClass

/**
 * 市场复盘数据模型
 */
@JsonClass(generateAdapter = true)
data class MarketReview(
    val date: String, // yyyy-MM-dd
    val indexPerformance: IndexPerformance,
    val marketSentiment: MarketSentiment,
    val capitalFlow: CapitalFlow,
    val marketStage: MarketStage,
    val sectorPerformances: List<SectorPerformance>,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 指数表现
     */
    @JsonClass(generateAdapter = true)
    data class IndexPerformance(
        val shanghaiIndex: IndexData, // 上证指数
        val shenzhenIndex: IndexData, // 深证成指
        val chinextIndex: IndexData,  // 创业板指
        val totalVolume: Double,      // 总成交量（亿元）
        val totalTurnover: Double     // 总成交额（亿元）
    )
    
    @JsonClass(generateAdapter = true)
    data class IndexData(
        val name: String,
        val currentPrice: Double,
        val change: Double,           // 涨跌点数
        val changeRate: Double,       // 涨跌幅（%）
        val volume: Double,           // 成交量
        val turnover: Double          // 成交额
    )
    
    /**
     * 市场情绪
     */
    @JsonClass(generateAdapter = true)
    data class MarketSentiment(
        val risingCount: Int,         // 上涨家数
        val fallingCount: Int,        // 下跌家数
        val flatCount: Int,           // 平盘家数
        val limitUpCount: Int,        // 涨停家数
        val limitDownCount: Int,      // 跌停家数
        val sentimentScore: Double    // 情绪得分（-1.0到1.0）
    )
    
    /**
     * 资金流向
     */
    @JsonClass(generateAdapter = true)
    data class CapitalFlow(
        val northboundFlow: Double,   // 北向资金净流入（亿元）
        val mainForceFlow: Double,    // 主力资金净流入（亿元）
        val retailFlow: Double        // 散户资金净流入（亿元）
    )
    
    /**
     * 市场阶段
     */
    enum class MarketStage {
        RISING,      // 主升
        CONSOLIDATION, // 震荡
        DECLINING,   // 退潮
        REBOUND      // 反弹
    }
}
