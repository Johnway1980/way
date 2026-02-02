package com.alphadoer.trader.domain.model

/**
 * 集合竞价观察领域模型
 */
data class AuctionObservation(
    val id: String,
    val date: String, // yyyy-MM-dd
    val marketSentiment: MarketSentiment, // 市场情绪
    val feeling: Int, // 感受评分 1-5
    val keyObservations: List<String>, // 关键观察点
    val volumeAnalysis: String?, // 成交量分析
    val priceTrend: PriceTrend, // 价格趋势
    val focusStocks: List<StockObservation>, // 重点股票观察
    val notes: String?, // 备注
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class MarketSentiment {
        BULLISH,    // 看涨
        BEARISH,    // 看跌
        NEUTRAL     // 中性
    }
    
    enum class PriceTrend {
        RISING,     // 上涨
        FALLING,    // 下跌
        FLAT        // 横盘
    }
    
    /**
     * 股票观察
     */
    data class StockObservation(
        val stockCode: String,
        val stockName: String,
        val openingPrice: Double?, // 开盘价
        val volume: Long?, // 成交量
        val change: Double?, // 涨跌幅
        val observation: String // 观察记录
    )
}
