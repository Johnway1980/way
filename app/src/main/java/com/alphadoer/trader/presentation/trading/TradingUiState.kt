package com.alphadoer.trader.presentation.trading

import com.alphadoer.trader.domain.model.RecommendedStock
import com.alphadoer.trader.domain.model.trading.TradeRecord
import com.alphadoer.trader.domain.model.trading.TradeStatistics

/**
 * 交易界面UI状态
 */
data class TradingUiState(
    val trades: List<TradeRecord> = emptyList(),
    val statistics: TradeStatistics? = null,
    val recommendedStocks: List<RecommendedStock> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showSuccessMessage: Boolean = false,
    val selectedTab: TradingTab = TradingTab.QUICK_RECORD,
    val adviceMessages: List<String> = emptyList()
) {
    enum class TradingTab {
        QUICK_RECORD,   // 快速记录
        TRADE_LIST,     // 交易列表
        STATISTICS      // 实时统计
    }
}
