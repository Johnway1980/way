package com.alphadoer.trader.presentation.review

import com.alphadoer.trader.domain.model.review.MistakePattern
import com.alphadoer.trader.domain.model.review.TradeMistake
import com.alphadoer.trader.domain.model.trading.TradeRecord

/**
 * 错误分析界面UI状态
 */
data class MistakeAnalysisUiState(
    val trades: List<TradeRecord> = emptyList(),
    val mistakes: List<TradeMistake> = emptyList(),
    val mistakePatterns: List<MistakePattern> = emptyList(),
    val selectedTrade: TradeRecord? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
