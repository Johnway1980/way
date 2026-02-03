package com.alphadoer.trader.domain.usecase.statistics

import com.alphadoer.trader.domain.model.statistics.TradingPerformance
import com.alphadoer.trader.domain.model.trading.TradeRecord
import com.alphadoer.trader.domain.model.trading.TradeStatistics
import com.alphadoer.trader.domain.repository.StatisticsRepository
import com.alphadoer.trader.domain.repository.TradeRecordRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 计算绩效指标用例
 */
class CalculatePerformanceMetricsUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val tradeRecordRepository: TradeRecordRepository
) {
    suspend operator fun invoke(startDate: String, endDate: String): Result<TradingPerformance> {
        return try {
            // 获取时间范围内的交易记录
            val allTrades = mutableListOf<TradeRecord>()
            val dateRange = generateDateRange(startDate, endDate)
            
            for (date in dateRange) {
                val trades = tradeRecordRepository.getTradesByDate(date).first()
                allTrades.addAll(trades)
            }
            
            // 计算绩效指标
            val performance = calculateMetrics(allTrades, startDate, endDate)
            
            Result.success(performance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun calculateMetrics(
        trades: List<TradeRecord>,
        startDate: String,
        endDate: String
    ): TradingPerformance {
        val totalProfitLoss = trades.sumOf { it.profitLoss ?: 0.0 }
        val totalTrades = trades.size
        val profitableTrades = trades.count { (it.profitLoss ?: 0.0) > 0 }
        val winRate = if (totalTrades > 0) (profitableTrades.toDouble() / totalTrades) * 100 else 0.0
        
        // 计算总收益率（简化计算）
        val totalReturn = if (totalTrades > 0) {
            (totalProfitLoss / (trades.sumOf { it.amount } / totalTrades)) * 100
        } else {
            0.0
        }
        
        // 计算盈亏比
        val profits = trades.filter { (it.profitLoss ?: 0.0) > 0 }.sumOf { it.profitLoss ?: 0.0 }
        val losses = trades.filter { (it.profitLoss ?: 0.0) < 0 }.sumOf { it.profitLoss ?: 0.0 }
        val profitLossRatio = if (losses != 0.0) profits / -losses else 0.0
        
        // 计算最大回撤（简化实现）
        val maxDrawdown = calculateMaxDrawdown(trades)
        
        // 计算平均持仓天数（简化实现）
        val averageHoldingDays = calculateAverageHoldingDays(trades)
        
        return TradingPerformance(
            period = "$startDate to $endDate",
            totalReturn = totalReturn,
            annualizedReturn = null, // TODO: 实现年化收益率计算
            totalProfitLoss = totalProfitLoss,
            sharpeRatio = null, // TODO: 实现夏普比率计算
            maxDrawdown = maxDrawdown,
            maxDrawdownDuration = 0, // TODO: 实现回撤持续时间计算
            winRate = winRate,
            profitLossRatio = profitLossRatio,
            totalTrades = totalTrades,
            averageHoldingDays = averageHoldingDays,
            volatility = null, // TODO: 实现波动率计算
            sortinoRatio = null,
            calmarRatio = null
        )
    }
    
    private fun calculateMaxDrawdown(trades: List<TradeRecord>): Double {
        // 简化实现：基于累计盈亏计算
        var maxEquity = 0.0
        var maxDrawdown = 0.0
        var currentEquity = 0.0
        
        trades.sortedBy { it.timestamp }.forEach { trade ->
            currentEquity += (trade.profitLoss ?: 0.0)
            if (currentEquity > maxEquity) {
                maxEquity = currentEquity
            }
            val drawdown = ((maxEquity - currentEquity) / maxEquity) * 100
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown
            }
        }
        
        return maxDrawdown
    }
    
    private fun calculateAverageHoldingDays(trades: List<TradeRecord>): Double {
        // 简化实现：基于交易时间戳计算
        if (trades.isEmpty()) return 0.0
        
        val buyTrades = trades.filter { it.isBuy() }
        val sellTrades = trades.filter { it.isSell() }
        
        if (buyTrades.isEmpty() || sellTrades.isEmpty()) return 0.0
        
        // 简化：使用平均时间差
        val avgBuyTime = buyTrades.map { it.timestamp }.average()
        val avgSellTime = sellTrades.map { it.timestamp }.average()
        
        return (avgSellTime - avgBuyTime) / (1000 * 60 * 60 * 24) // 转换为天数
    }
    
    private fun generateDateRange(startDate: String, endDate: String): List<String> {
        // 简化实现：返回日期列表
        // TODO: 实现完整的日期范围生成
        return listOf(startDate, endDate)
    }
}
