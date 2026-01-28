package com.alphadoer.trader.domain.repository

import com.alphadoer.trader.domain.model.trading.TradeRecord
import com.alphadoer.trader.domain.model.trading.TradeStatistics
import kotlinx.coroutines.flow.Flow

/**
 * 交易记录Repository接口
 */
interface TradeRecordRepository {
    
    /**
     * 保存交易记录
     */
    suspend fun saveTradeRecord(record: TradeRecord): Result<Unit>
    
    /**
     * 更新交易记录
     */
    suspend fun updateTradeRecord(record: TradeRecord): Result<Unit>
    
    /**
     * 删除交易记录
     */
    suspend fun deleteTradeRecord(id: String): Result<Unit>
    
    /**
     * 根据ID获取交易记录
     */
    suspend fun getTradeRecordById(id: String): TradeRecord?
    
    /**
     * 获取指定日期的交易记录
     */
    fun getTradesByDate(date: String): Flow<List<TradeRecord>>
    
    /**
     * 获取指定股票的交易记录
     */
    fun getTradesByStock(stockCode: String, date: String? = null): Flow<List<TradeRecord>>
    
    /**
     * 获取交易统计
     */
    suspend fun getTradeStatistics(date: String): TradeStatistics
    
    /**
     * 计算盈亏
     */
    suspend fun calculateProfitLoss(record: TradeRecord): Double?
}
