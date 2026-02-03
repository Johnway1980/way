package com.alphadoer.trader.domain.model.trading

import com.squareup.moshi.JsonClass

/**
 * 交易统计模型
 */
@JsonClass(generateAdapter = true)
data class TradeStatistics(
    val date: String, // yyyy-MM-dd
    val totalTrades: Int = 0, // 总交易次数
    val buyCount: Int = 0, // 买入次数
    val sellCount: Int = 0, // 卖出次数
    val totalProfitLoss: Double = 0.0, // 总盈亏
    val totalProfitLossRate: Double = 0.0, // 总盈亏率
    val winCount: Int = 0, // 盈利交易次数
    val lossCount: Int = 0, // 亏损交易次数
    val winRate: Double = 0.0, // 胜率
    val totalAmount: Double = 0.0, // 总成交金额
    val totalCommission: Double = 0.0, // 总手续费
    val positions: List<Position> = emptyList(), // 当前持仓
    val capitalUsed: Double = 0.0, // 已使用资金
    val availableCapital: Double = 0.0 // 可用资金
) {
    /**
     * 持仓信息
     */
    @JsonClass(generateAdapter = true)
    data class Position(
        val stockCode: String,
        val stockName: String,
        val quantity: Int, // 持仓数量
        val averagePrice: Double, // 平均成本价
        val currentPrice: Double? = null, // 当前市价
        val marketValue: Double? = null, // 市值
        val profitLoss: Double? = null, // 浮动盈亏
        val profitLossRate: Double? = null // 盈亏率
    )
    
    /**
     * 计算胜率
     */
    fun calculateWinRate(): Double {
        val totalClosed = winCount + lossCount
        return if (totalClosed > 0) {
            winCount.toDouble() / totalClosed * 100
        } else {
            0.0
        }
    }
}
