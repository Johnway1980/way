package com.alphadoer.trader.domain.model.review

import com.squareup.moshi.JsonClass

/**
 * 个股深度分析
 */
@JsonClass(generateAdapter = true)
data class StockDeepAnalysis(
    val stockCode: String,
    val stockName: String,
    val date: String, // yyyy-MM-dd
    val fundamentalAnalysis: FundamentalAnalysis,
    val technicalAnalysis: TechnicalAnalysis,
    val capitalAnalysis: CapitalAnalysis,
    val catalystAnalysis: CatalystAnalysis,
    val overallRating: AnalysisRating, // 综合评级
    val recommendation: String?,       // 投资建议
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 基本面分析
     */
    @JsonClass(generateAdapter = true)
    data class FundamentalAnalysis(
        val peRatio: Double?,          // 市盈率
        val pbRatio: Double?,          // 市净率
        val roe: Double?,              // ROE
        val grossMargin: Double?,      // 毛利率
        val industryRanking: Int?,     // 行业排名
        val valuationPercentile: Double?, // 估值分位（0-100）
        val growthRate: Double?,       // 成长率
        val competitiveAdvantage: String? // 竞争优势
    )
    
    /**
     * 技术面分析
     */
    @JsonClass(generateAdapter = true)
    data class TechnicalAnalysis(
        val pattern: String?,          // K线形态
        val supportLevel: Double?,     // 支撑位
        val resistanceLevel: Double?,  // 阻力位
        val macdSignal: String?,      // MACD信号
        val kdjSignal: String?,       // KDJ信号
        val rsi: Double?,             // RSI值
        val trend: TechnicalTrend,     // 趋势判断
        val volumeAnalysis: String?   // 成交量分析
    )
    
    /**
     * 资金面分析
     */
    @JsonClass(generateAdapter = true)
    data class CapitalAnalysis(
        val mainForceFlow: Double,     // 主力资金流向
        val retailFlow: Double,        // 散户资金流向
        val northboundFlow: Double?,   // 北向资金流向
        val institutionalHoldings: Double?, // 机构持仓比例
        val shareholdingConcentration: Double? // 持股集中度
    )
    
    /**
     * 技术趋势
     */
    enum class TechnicalTrend {
        STRONG_UPTREND,    // 强势上涨
        UPTREND,           // 上涨趋势
        CONSOLIDATION,     // 震荡整理
        DOWNTREND,         // 下跌趋势
        STRONG_DOWNTREND   // 强势下跌
    }
    
    /**
     * 分析评级
     */
    enum class AnalysisRating {
        STRONG_BUY,        // 强烈买入
        BUY,               // 买入
        HOLD,              // 持有
        SELL,              // 卖出
        STRONG_SELL        // 强烈卖出
    }
}
