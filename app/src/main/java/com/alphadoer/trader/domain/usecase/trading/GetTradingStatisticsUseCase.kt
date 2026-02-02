package com.alphadoer.trader.domain.usecase.trading

import com.alphadoer.trader.domain.model.trading.TradeOperation
import com.alphadoer.trader.domain.model.trading.TradeRecord
import com.alphadoer.trader.domain.model.trading.TradeStatistics
import com.alphadoer.trader.domain.repository.TradeRecordRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 获取交易统计用例
 */
class GetTradingStatisticsUseCase @Inject constructor(
    private val tradeRecordRepository: TradeRecordRepository
) {
    suspend operator fun invoke(date: String): TradeStatistics {
        val trades = tradeRecordRepository.getTradesByDate(date)
            .first()
        
        return calculateStatistics(trades, date)
    }
    
    private fun calculateStatistics(trades: List<TradeRecord>, date: String): TradeStatistics {
        val buyCount = trades.count { it.operation == TradeOperation.BUY }
        val sellCount = trades.count { it.operation == TradeOperation.SELL }
        val totalTrades = trades.size
        
        val totalAmount = trades.sumOf { it.amount }
        val totalCommission = trades.sumOf { it.commission }
        
        // 计算盈亏
        val closedTrades = trades.filter { it.profitLoss != null }
        val winCount = closedTrades.count { (it.profitLoss ?: 0.0) > 0 }
        val lossCount = closedTrades.count { (it.profitLoss ?: 0.0) < 0 }
        val totalProfitLoss = closedTrades.sumOf { it.profitLoss ?: 0.0 }
        
        // 计算持仓
        val positions = calculatePositions(trades)
        val capitalUsed = positions.sumOf { it.averagePrice * it.quantity }
        
        return TradeStatistics(
            date = date,
            totalTrades = totalTrades,
            buyCount = buyCount,
            sellCount = sellCount,
            totalProfitLoss = totalProfitLoss,
            totalProfitLossRate = if (capitalUsed > 0) (totalProfitLoss / capitalUsed) * 100 else 0.0,
            winCount = winCount,
            lossCount = lossCount,
            winRate = if (closedTrades.isNotEmpty()) (winCount.toDouble() / closedTrades.size) * 100 else 0.0,
            totalAmount = totalAmount,
            totalCommission = totalCommission,
            positions = positions,
            capitalUsed = capitalUsed,
            availableCapital = 0.0 // TODO: 从账户信息获取
        )
    }
    
    private fun calculatePositions(trades: List<TradeRecord>): List<TradeStatistics.Position> {
        val positionMap = mutableMapOf<String, MutableList<TradeRecord>>()
        
        // 按股票分组
        trades.forEach { trade ->
            positionMap.getOrPut(trade.stockCode) { mutableListOf() }.add(trade)
        }
        
        return positionMap.mapNotNull { (stockCode, stockTrades) ->
            val buyTrades = stockTrades.filter { it.operation == TradeOperation.BUY }
            val sellTrades = stockTrades.filter { it.operation == TradeOperation.SELL }
            
            val totalBuyQuantity = buyTrades.sumOf { it.quantity }
            val totalSellQuantity = sellTrades.sumOf { it.quantity }
            val remainingQuantity = totalBuyQuantity - totalSellQuantity
            
            if (remainingQuantity > 0) {
                val totalCost = buyTrades.sumOf { it.price * it.quantity + it.commission }
                val averagePrice = totalCost / totalBuyQuantity
                
                TradeStatistics.Position(
                    stockCode = stockCode,
                    stockName = buyTrades.firstOrNull()?.stockName ?: "",
                    quantity = remainingQuantity,
                    averagePrice = averagePrice,
                    currentPrice = null, // TODO: 从行情数据获取
                    marketValue = null,
                    profitLoss = null,
                    profitLossRate = null
                )
            } else {
                null
            }
        }
    }
}
