package com.alphadoer.trader.domain.model.process

/**
 * 步骤类型枚举（对应8个交易步骤）
 */
enum class StepType(val stepNumber: Int, val displayName: String) {
    MORNING_READING(1, "早间信息阅读"),
    PRE_MARKET_PLAN(2, "盘前计划"),
    AUCTION_OBSERVATION(3, "集合竞价观察"),
    TRADING(4, "盘中交易"),
    POST_TRADING_REVIEW(5, "盘后复盘"),
    MISTAKE_ANALYSIS(6, "错误分析"),
    IMPROVEMENT_PLAN(7, "改进计划"),
    NEXT_DAY_PREP(8, "次日准备")
}
