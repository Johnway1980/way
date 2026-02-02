package com.alphadoer.trader.presentation.review

import com.alphadoer.trader.domain.model.review.DailyReviewSummary
import com.alphadoer.trader.domain.model.review.ImprovementPlan

/**
 * 复盘总结界面UI状态
 */
data class ReviewSummaryUiState(
    val summary: DailyReviewSummary? = null,
    val tomorrowPlan: ImprovementPlan? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
