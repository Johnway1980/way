package com.alphadoer.trader.domain.model.review

import com.squareup.moshi.JsonClass

/**
 * 错误统计
 */
@JsonClass(generateAdapter = true)
data class MistakeStatistics(
    val dateRange: String,             // 统计时间范围
    val totalMistakes: Int,            // 总错误数
    val totalLoss: Double,              // 总损失金额
    val mistakeByType: Map<TradeMistake.MistakeType, Int>, // 按类型统计
    val mistakeByCategory: Map<TradeMistake.MistakeCategory, Int>, // 按分类统计
    val timeDistribution: Map<String, Int>, // 时间分布（按小时/日期）
    val topMistakes: List<MistakePattern>, // 高频错误
    val improvementTrend: ImprovementTrend? // 改进趋势
) {
    /**
     * 改进趋势
     */
    @JsonClass(generateAdapter = true)
    data class ImprovementTrend(
        val period: String,             // 时间段
        val mistakeCount: List<Int>,    // 各期错误数量
        val lossAmount: List<Double>,   // 各期损失金额
        val trend: TrendDirection       // 趋势方向
    )
    
    enum class TrendDirection {
        IMPROVING,      // 改善中
        STABLE,         // 稳定
        DETERIORATING   // 恶化
    }
}
