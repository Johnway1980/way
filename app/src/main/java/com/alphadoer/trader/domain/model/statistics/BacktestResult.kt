package com.alphadoer.trader.domain.model.statistics

import com.squareup.moshi.JsonClass

/**
 * 回测结果
 */
@JsonClass(generateAdapter = true)
data class BacktestResult(
    val configId: String,
    val startDate: String,
    val endDate: String,
    val finalCapital: Double, // 最终资金
    val totalReturn: Double, // 总收益率（%）
    val annualizedReturn: Double, // 年化收益率（%）
    val maxDrawdown: Double, // 最大回撤（%）
    val sharpeRatio: Double, // 夏普比率
    val winRate: Double, // 胜率
    val totalTrades: Int, // 总交易次数
    val equityCurve: List<EquityPoint>, // 权益曲线
    val tradeDetails: List<TradeDetail>, // 交易明细
    val riskMetrics: RiskMetrics, // 风险指标
    val benchmarkComparison: BenchmarkComparison?, // 基准对比
    val createdAt: Long = System.currentTimeMillis()
) {
    @JsonClass(generateAdapter = true)
    data class EquityPoint(
        val date: String, // yyyy-MM-dd
        val equity: Double, // 权益
        val returnRate: Double // 收益率
    )
    
    @JsonClass(generateAdapter = true)
    data class TradeDetail(
        val date: String,
        val stockCode: String,
        val stockName: String,
        val action: String, // BUY/SELL
        val price: Double,
        val quantity: Int,
        val profitLoss: Double?
    )
    
    @JsonClass(generateAdapter = true)
    data class RiskMetrics(
        val volatility: Double, // 波动率
        val downsideDeviation: Double, // 下行波动率
        val var95: Double?, // 95% VaR
        val cvar95: Double?, // 95% CVaR
        val maxDrawdownDuration: Int // 最大回撤持续时间
    )
    
    @JsonClass(generateAdapter = true)
    data class BenchmarkComparison(
        val benchmarkReturn: Double, // 基准收益率
        val alpha: Double, // Alpha
        val beta: Double, // Beta
        val trackingError: Double // 跟踪误差
    )
}
