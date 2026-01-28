package com.alphadoer.trader.domain.model.review

import com.squareup.moshi.JsonClass

/**
 * 信息催化剂分析
 */
@JsonClass(generateAdapter = true)
data class CatalystAnalysis(
    val stockCode: String,
    val date: String,
    val catalysts: List<Catalyst>,
    val overallImpact: ImpactLevel,   // 综合影响
    val credibility: Double           // 可信度（0.0-1.0）
) {
    /**
     * 催化剂
     */
    @JsonClass(generateAdapter = true)
    data class Catalyst(
        val type: CatalystType,
        val content: String,
        val source: String,           // 消息来源
        val timestamp: Long,
        val impact: ImpactLevel,
        val timeHorizon: TimeHorizon, // 影响时效
        val credibility: Double,     // 可信度
        val relatedStocks: List<String> // 关联股票
    )
    
    /**
     * 催化剂类型
     */
    enum class CatalystType {
        POSITIVE_NEWS,    // 利好消息
        NEGATIVE_NEWS,    // 利空消息
        POLICY,           // 政策影响
        INDUSTRY,         // 行业变化
        COMPANY,          // 公司公告
        RUMOR             // 传闻
    }
    
    /**
     * 影响级别
     */
    enum class ImpactLevel {
        HIGH,             // 高影响
        MEDIUM,           // 中等影响
        LOW,              // 低影响
        NEGLIGIBLE        // 可忽略
    }
    
    /**
     * 影响时效
     */
    enum class TimeHorizon {
        SHORT_TERM,       // 短期（1-3天）
        MEDIUM_TERM,      // 中期（1-2周）
        LONG_TERM         // 长期（1个月以上）
    }
}
