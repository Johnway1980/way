package com.alphadoer.trader.domain.model.review

import com.squareup.moshi.JsonClass

/**
 * 当日复盘总结
 */
@JsonClass(generateAdapter = true)
data class DailyReviewSummary(
    val date: String, // yyyy-MM-dd
    val successExperiences: List<String>,  // 成功经验
    val failureLessons: List<String>,      // 失败教训
    val marketInsights: String?,            // 市场认知
    val keyObservations: List<String>,     // 关键观察
    val tomorrowPlan: ImprovementPlan?,    // 明日计划
    val overallRating: String?,           // 总体评价
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
