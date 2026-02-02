package com.alphadoer.trader.data.repository

import com.alphadoer.trader.data.local.dao.TradeRecordDao
import com.alphadoer.trader.data.local.entity.TradeRecordEntity
import com.alphadoer.trader.domain.model.trading.TradeOperation
import com.alphadoer.trader.domain.model.trading.TradeRecord
import com.alphadoer.trader.domain.model.trading.TradeStatistics
import com.alphadoer.trader.domain.model.trading.TradeStatus
import com.alphadoer.trader.domain.repository.TradeRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 交易记录Repository实现
 */
class TradeRecordRepositoryImpl @Inject constructor(
    private val tradeRecordDao: TradeRecordDao
) : TradeRecordRepository {
    
    override suspend fun saveTradeRecord(record: TradeRecord): Result<Unit> {
        return try {
            val entity = record.toEntity()
            tradeRecordDao.insertRecord(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateTradeRecord(record: TradeRecord): Result<Unit> {
        return try {
            val entity = record.toEntity()
            tradeRecordDao.updateRecord(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteTradeRecord(id: String): Result<Unit> {
        return try {
            tradeRecordDao.deleteRecordById(id.toLongOrNull() ?: return Result.failure(IllegalArgumentException("Invalid ID")))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTradeRecordById(id: String): TradeRecord? {
        val entity = tradeRecordDao.getRecordById(id.toLongOrNull() ?: return null)
        return entity?.toDomainModel()
    }
    
    override fun getTradesByDate(date: String): Flow<List<TradeRecord>> {
        return tradeRecordDao.getRecordsByDateFlow(date)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override fun getTradesByStock(stockCode: String, date: String?): Flow<List<TradeRecord>> {
        return if (date != null) {
            tradeRecordDao.getRecordsByDateFlow(date)
                .map { entities -> 
                    entities.filter { it.stockCode == stockCode }
                        .map { it.toDomainModel() }
                }
        } else {
            tradeRecordDao.getRecordsByStockCodeFlow(stockCode)
                .map { entities -> entities.map { it.toDomainModel() } }
        }
    }
    
    override suspend fun getTradeStatistics(date: String): TradeStatistics {
        val trades = tradeRecordDao.getRecordsByDate(date)
            .map { it.toDomainModel() }
        
        return calculateStatistics(trades, date)
    }
    
    override suspend fun calculateProfitLoss(record: TradeRecord): Double? {
        if (record.operation != TradeOperation.SELL) {
            return null
        }
        
        // 获取该股票的买入记录
        val buyEntities = tradeRecordDao.getRecordsByStockCode(record.stockCode)
            .filter { it.tradeType == "BUY" }
            .sortedBy { it.tradeTime }
        val buyRecords = buyEntities.map { it.toDomainModel() }
        
        // 使用FIFO计算盈亏
        var remainingQuantity = record.quantity
        var totalCost = 0.0
        
        for (buyRecord in buyRecords) {
            if (remainingQuantity <= 0) break
            
            val usedQuantity = minOf(remainingQuantity, buyRecord.quantity)
            totalCost += buyRecord.price * usedQuantity + buyRecord.commission
            remainingQuantity -= usedQuantity
        }
        
        if (remainingQuantity > 0) {
            // 卖出数量超过买入数量
            return null
        }
        
        val sellAmount = record.price * record.quantity - record.commission
        return sellAmount - totalCost
    }
    
    // ========== 数据转换 ==========
    private fun TradeRecord.toEntity(): TradeRecordEntity {
        return TradeRecordEntity.create(
            journalDate = date,
            stockCode = stockCode,
            stockName = stockName,
            tradeType = operation.name,
            price = price,
            quantity = quantity,
            tradeTime = timestamp,
            profit = profitLoss,
            profitRate = profitLossRate,
            notes = notes
        ).copy(
            id = id.toLongOrNull() ?: 0L,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    private fun TradeRecordEntity.toDomainModel(): TradeRecord {
        return TradeRecord(
            id = id.toString(),
            date = journalDate,
            stockCode = stockCode,
            stockName = stockName,
            operation = TradeOperation.valueOf(tradeType),
            status = TradeStatus.EXECUTED, // 默认已执行
            price = price,
            quantity = quantity,
            amount = price * quantity,
            commission = 0.0, // TODO: 从实体中获取
            timestamp = tradeTime,
            reason = null,
            relatedAnalysisId = null,
            stopLoss = null,
            takeProfit = null,
            tags = emptyList(),
            emotion = null,
            images = emptyList(),
            notes = notes,
            profitLoss = profit,
            profitLossRate = profitRate,
            createdAt = createdAt ?: System.currentTimeMillis(),
            updatedAt = updatedAt ?: System.currentTimeMillis()
        )
    }
    
    private fun calculateStatistics(trades: List<TradeRecord>, date: String): TradeStatistics {
        val buyCount = trades.count { it.operation == TradeOperation.BUY }
        val sellCount = trades.count { it.operation == TradeOperation.SELL }
        val totalTrades = trades.size
        
        val totalAmount = trades.sumOf { it.amount }
        val totalCommission = trades.sumOf { it.commission }
        
        val closedTrades = trades.filter { it.profitLoss != null }
        val winCount = closedTrades.count { (it.profitLoss ?: 0.0) > 0 }
        val lossCount = closedTrades.count { (it.profitLoss ?: 0.0) < 0 }
        val totalProfitLoss = closedTrades.sumOf { it.profitLoss ?: 0.0 }
        
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
            availableCapital = 0.0
        )
    }
    
    private fun calculatePositions(trades: List<TradeRecord>): List<TradeStatistics.Position> {
        val positionMap = mutableMapOf<String, MutableList<TradeRecord>>()
        
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
                    currentPrice = null,
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
