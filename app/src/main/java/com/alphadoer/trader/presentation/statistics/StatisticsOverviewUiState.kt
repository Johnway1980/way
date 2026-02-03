package com.alphadoer.trader.presentation.statistics

import com.alphadoer.trader.domain.model.statistics.TradingPerformance

/**
 * 统计概览界面UI状态
 */
data class StatisticsOverviewUiState(
    val performance: TradingPerformance? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
