package com.alphadoer.trader.domain.model.review

import com.squareup.moshi.JsonClass

/**
 * 强势股排行榜
 */
@JsonClass(generateAdapter = true)
data class StockLeaderboard(
    val date: String, // yyyy-MM-dd
    val limitUpStocks: List<LimitUpStock>,      // 涨停股
    val volumeAnomalyStocks: List<VolumeAnomalyStock>, // 成交量异动股
    val capitalInflowStocks: List<CapitalInflowStock>  // 资金流入股
) {
    /**
     * 涨停股
     */
    @JsonClass(generateAdapter = true)
    data class LimitUpStock(
        val stockCode: String,
        val stockName: String,
        val consecutiveDays: Int,     // 连板天数
        val sealAmount: Double,       // 封单金额（万元）
        val concepts: List<String>,    // 概念题材
        val sector: String,           // 所属板块
        val firstLimitUpTime: String  // 首次涨停时间
    )
    
    /**
     * 成交量异动股
     */
    @JsonClass(generateAdapter = true)
    data class VolumeAnomalyStock(
        val stockCode: String,
        val stockName: String,
        val volumeRatio: Double,      // 量比
        val turnoverRate: Double,      // 换手率（%）
        val priceChange: Double,       // 涨跌幅（%）
        val volume: Double             // 成交量
    )
    
    /**
     * 资金流入股
     */
    @JsonClass(generateAdapter = true)
    data class CapitalInflowStock(
        val stockCode: String,
        val stockName: String,
        val netInflow: Double,         // 净流入（万元）
        val mainForceInflow: Double,   // 主力净流入（万元）
        val priceChange: Double,       // 涨跌幅（%）
        val sector: String            // 所属板块
    )
}
