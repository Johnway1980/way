package com.alphadoer.trader.domain.model.trading

import com.squareup.moshi.JsonClass
import java.util.UUID

/**
 * 交易记录模型
 */
@JsonClass(generateAdapter = true)
data class TradeRecord(
    val id: String = UUID.randomUUID().toString(),
    val date: String, // yyyy-MM-dd
    val stockCode: String,
    val stockName: String,
    val operation: TradeOperation,
    val status: TradeStatus,
    val price: Double,
    val quantity: Int,
    val amount: Double, // 成交金额 = price * quantity
    val commission: Double = 0.0, // 手续费
    val timestamp: Long = System.currentTimeMillis(), // 交易时间戳
    val reason: String? = null, // 交易理由
    val relatedAnalysisId: String? = null, // 关联的早间分析ID
    val stopLoss: Double? = null, // 止损价
    val takeProfit: Double? = null, // 止盈价
    val tags: List<String> = emptyList(), // 交易标签
    val emotion: Emotion? = null, // 交易时情绪
    val images: List<String> = emptyList(), // 关联的图片路径
    val notes: String? = null, // 备注
    val profitLoss: Double? = null, // 盈亏（卖出时计算）
    val profitLossRate: Double? = null, // 盈亏率
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 情绪状态
     */
    @JsonClass(generateAdapter = true)
    enum class Emotion {
        CONFIDENT,      // 自信
        CAUTIOUS,       // 谨慎
        ANXIOUS,        // 焦虑
        EXCITED,        // 兴奋
        CALM,           // 平静
        REGRETFUL       // 后悔
    }
    
    /**
     * 计算成交金额
     */
    fun calculateAmount(): Double {
        return price * quantity + commission
    }
    
    /**
     * 是否为买入操作
     */
    fun isBuy(): Boolean = operation == TradeOperation.BUY
    
    /**
     * 是否为卖出操作
     */
    fun isSell(): Boolean = operation == TradeOperation.SELL
}
