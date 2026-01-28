package com.alphadoer.trader.presentation.premarketplan

import com.alphadoer.trader.domain.model.PreMarketPlan
import com.alphadoer.trader.domain.model.RecommendedStock

/**
 * 盘前计划UI状态
 */
data class PreMarketPlanUiState(
    val plan: PreMarketPlan? = null,
    val recommendedStocks: List<RecommendedStock> = emptyList(),
    val focusStocks: List<PreMarketPlan.FocusStock> = emptyList(),
    val tradingStrategy: String = "",
    val riskControl: String = "",
    val marketOutlook: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)
