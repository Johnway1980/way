package com.alphadoer.trader.domain.model

/**
 * 盘前计划领域模型
 */
data class PreMarketPlan(
    val id: String,
    val date: String, // yyyy-MM-dd
    val focusStocks: List<FocusStock>, // 重点关注股票
    val tradingStrategy: String?, // 交易策略
    val riskControl: String?, // 风险控制
    val marketOutlook: String?, // 市场展望
    val notes: String?, // 备注
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 重点关注股票
     */
    data class FocusStock(
        val stockCode: String,
        val stockName: String,
        val reason: String, // 关注原因
        val targetPrice: Double? = null, // 目标价格
        val stopLossPrice: Double? = null, // 止损价格
        val action: StockAction = StockAction.WATCH // 计划操作
    ) {
        enum class StockAction {
            BUY,        // 买入
            SELL,       // 卖出
            WATCH,      // 观察
            HOLD        // 持有
        }
    }
}
