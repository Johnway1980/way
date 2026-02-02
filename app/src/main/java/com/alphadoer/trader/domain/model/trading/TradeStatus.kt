package com.alphadoer.trader.domain.model.trading

/**
 * 交易状态枚举
 */
enum class TradeStatus {
    PENDING,    // 待执行
    EXECUTED,   // 已执行
    CANCELLED,  // 已取消
    SETTLED     // 已结算
}
