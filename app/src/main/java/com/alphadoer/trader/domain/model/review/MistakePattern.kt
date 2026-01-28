package com.alphadoer.trader.domain.model.review

import com.squareup.moshi.JsonClass

/**
 * 错误模式
 */
@JsonClass(generateAdapter = true)
data class MistakePattern(
    val id: String,
    val category: TradeMistake.MistakeCategory,
    val frequency: Int,               // 发生频率
    val totalLoss: Double,             // 累计损失
    val averageLoss: Double,           // 平均损失
    val relatedMistakes: List<String>, // 关联的错误ID列表
    val commonContext: String,         // 常见情境
    val improvementMeasures: List<String>, // 改进措施
    val effectiveness: Double?,        // 改进效果（错误重复率降低）
    val lastOccurrence: Long,          // 最后发生时间
    val createdAt: Long = System.currentTimeMillis()
)
