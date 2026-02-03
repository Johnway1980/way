package com.alphadoer.trader.domain.model.statistics

import com.squareup.moshi.JsonClass

/**
 * 数据洞察
 */
@JsonClass(generateAdapter = true)
data class StatisticalInsight(
    val id: String,
    val insightType: InsightType,
    val title: String,
    val description: String,
    val importance: Importance,
    val evidence: List<String>, // 证据数据
    val recommendation: String?, // 建议
    val relatedMetrics: List<String>, // 相关指标
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class InsightType {
        PERFORMANCE,    // 绩效洞察
        RISK,           // 风险洞察
        BEHAVIOR,       // 行为洞察
        PATTERN,        // 模式洞察
        OPPORTUNITY     // 机会洞察
    }
    
    enum class Importance {
        HIGH,      // 高
        MEDIUM,    // 中
        LOW        // 低
    }
}
