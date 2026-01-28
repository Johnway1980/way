package com.alphadoer.trader.domain.model.statistics

import com.squareup.moshi.JsonClass

/**
 * 回测配置
 */
@JsonClass(generateAdapter = true)
data class BacktestConfig(
    val id: String,
    val name: String,
    val startDate: String, // yyyy-MM-dd
    val endDate: String, // yyyy-MM-dd
    val initialCapital: Double, // 初始资金
    val commissionRate: Double, // 手续费率（%）
    val slippage: Double, // 滑点（%）
    val maxPosition: Int?, // 最大持仓数量
    val maxSinglePosition: Double?, // 单只股票最大仓位（%）
    val strategyRules: List<StrategyRule>, // 策略规则
    val benchmark: String?, // 基准指数
    val createdAt: Long = System.currentTimeMillis()
) {
    @JsonClass(generateAdapter = true)
    data class StrategyRule(
        val type: RuleType,
        val condition: String, // 条件表达式
        val action: String // 执行动作
    )
    
    enum class RuleType {
        ENTRY,  // 入场规则
        EXIT,   // 出场规则
        POSITION // 仓位规则
    }
}
