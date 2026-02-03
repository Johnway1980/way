package com.alphadoer.trader.domain.model.statistics

import com.squareup.moshi.JsonClass

/**
 * 交易行为模式
 */
@JsonClass(generateAdapter = true)
data class TradingPattern(
    val id: String,
    val patternType: PatternType,
    val name: String,
    val description: String,
    val confidence: Double, // 置信度（0.0-1.0）
    val frequency: Int, // 出现频率
    val averageReturn: Double, // 平均收益
    val winRate: Double, // 胜率
    val characteristics: Map<String, String>, // 模式特征
    val sampleSize: Int, // 样本数量
    val validationResult: ValidationResult?, // 验证结果
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class PatternType {
        SUCCESS_PATTERN,    // 成功模式
        FAILURE_PATTERN,    // 失败模式
        BEHAVIOR_PATTERN,   // 行为模式
        MARKET_PATTERN      // 市场模式
    }
    
    @JsonClass(generateAdapter = true)
    data class ValidationResult(
        val inSamplePerformance: Double, // 样本内表现
        val outSamplePerformance: Double?, // 样本外表现
        val statisticalSignificance: Double, // 统计显著性
        val isValid: Boolean // 是否有效
    )
}
