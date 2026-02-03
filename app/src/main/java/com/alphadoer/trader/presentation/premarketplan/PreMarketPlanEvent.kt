package com.alphadoer.trader.presentation.premarketplan

import com.alphadoer.trader.domain.model.PreMarketPlan

/**
 * 盘前计划事件
 */
sealed class PreMarketPlanEvent {
    data class AddFocusStock(val stock: PreMarketPlan.FocusStock) : PreMarketPlanEvent()
    data class RemoveFocusStock(val stockCode: String) : PreMarketPlanEvent()
    data class UpdateFocusStock(val stock: PreMarketPlan.FocusStock) : PreMarketPlanEvent()
    data class TradingStrategyChanged(val strategy: String) : PreMarketPlanEvent()
    data class RiskControlChanged(val riskControl: String) : PreMarketPlanEvent()
    data class MarketOutlookChanged(val outlook: String) : PreMarketPlanEvent()
    data class NotesChanged(val notes: String) : PreMarketPlanEvent()
    object SavePlan : PreMarketPlanEvent()
    object LoadPlan : PreMarketPlanEvent()
    object ClearError : PreMarketPlanEvent()
}
