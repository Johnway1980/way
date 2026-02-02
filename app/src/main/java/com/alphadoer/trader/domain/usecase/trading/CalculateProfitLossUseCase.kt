package com.alphadoer.trader.domain.usecase.trading

import com.alphadoer.trader.domain.model.trading.TradeOperation
import com.alphadoer.trader.domain.model.trading.TradeRecord
import com.alphadoer.trader.domain.repository.TradeRecordRepository
import javax.inject.Inject

/**
 * 计算交易盈亏用例
 */
class CalculateProfitLossUseCase @Inject constructor(
    private val tradeRecordRepository: TradeRecordRepository
) {
    suspend operator fun invoke(sellRecord: TradeRecord, buyRecords: List<TradeRecord>): Double? {
        if (sellRecord.operation != TradeOperation.SELL) {
            return null
        }
        
        // 使用FIFO（先进先出）原则计算盈亏
        var remainingQuantity = sellRecord.quantity
        var totalCost = 0.0
        var totalBuyQuantity = 0
        
        for (buyRecord in buyRecords.sortedBy { it.timestamp }) {
            if (remainingQuantity <= 0) break
            
            val usedQuantity = minOf(remainingQuantity, buyRecord.quantity)
            totalCost += buyRecord.price * usedQuantity + buyRecord.commission
            totalBuyQuantity += usedQuantity
            remainingQuantity -= usedQuantity
        }
        
        if (totalBuyQuantity < sellRecord.quantity) {
            // 卖出数量超过买入数量，无法计算准确盈亏
            return null
        }
        
        val sellAmount = sellRecord.price * sellRecord.quantity - sellRecord.commission
        val profitLoss = sellAmount - totalCost
        
        return profitLoss
    }
    
    /**
     * 计算单笔交易的盈亏率
     */
    fun calculateProfitLossRate(profitLoss: Double, cost: Double): Double {
        return if (cost > 0) {
            (profitLoss / cost) * 100
        } else {
            0.0
        }
    }
}
