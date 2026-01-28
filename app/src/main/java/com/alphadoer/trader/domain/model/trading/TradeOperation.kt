package com.alphadoer.trader.domain.model.trading

/**
 * 交易操作类型枚举
 */
enum class TradeOperation {
    BUY,        // 买入
    SELL,       // 卖出
    HOLD,       // 持有（不操作）
    CANCEL      // 取消
}
