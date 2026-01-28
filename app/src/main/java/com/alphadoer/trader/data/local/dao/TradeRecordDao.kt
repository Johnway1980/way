package com.alphadoer.trader.data.local.dao

import androidx.room.*
import com.alphadoer.trader.data.local.entity.TradeRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 交易记录DAO
 */
@Dao
interface TradeRecordDao {
    
    @Query("SELECT * FROM trade_record ORDER BY tradeTime DESC")
    fun getAllRecords(): Flow<List<TradeRecordEntity>>
    
    @Query("SELECT * FROM trade_record WHERE id = :id")
    suspend fun getRecordById(id: Long): TradeRecordEntity?
    
    @Query("SELECT * FROM trade_record WHERE journalDate = :date ORDER BY tradeTime DESC")
    suspend fun getRecordsByDate(date: String): List<TradeRecordEntity>
    
    @Query("SELECT * FROM trade_record WHERE journalDate = :date ORDER BY tradeTime DESC")
    fun getRecordsByDateFlow(date: String): Flow<List<TradeRecordEntity>>
    
    @Query("SELECT * FROM trade_record WHERE stockCode = :stockCode ORDER BY tradeTime DESC")
    suspend fun getRecordsByStockCode(stockCode: String): List<TradeRecordEntity>
    
    @Query("SELECT * FROM trade_record WHERE stockCode = :stockCode ORDER BY tradeTime DESC")
    fun getRecordsByStockCodeFlow(stockCode: String): Flow<List<TradeRecordEntity>>
    
    @Query("SELECT * FROM trade_record WHERE journalDate BETWEEN :startDate AND :endDate ORDER BY tradeTime DESC")
    suspend fun getRecordsByDateRange(startDate: String, endDate: String): List<TradeRecordEntity>
    
    @Query("SELECT * FROM trade_record WHERE tradeType = :tradeType ORDER BY tradeTime DESC")
    suspend fun getRecordsByTradeType(tradeType: String): List<TradeRecordEntity>
    
    @Query("SELECT SUM(profit) FROM trade_record WHERE journalDate = :date AND profit IS NOT NULL")
    suspend fun getTotalProfitByDate(date: String): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TradeRecordEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<TradeRecordEntity>)
    
    @Update
    suspend fun updateRecord(record: TradeRecordEntity)
    
    @Delete
    suspend fun deleteRecord(record: TradeRecordEntity)
    
    @Query("DELETE FROM trade_record WHERE id = :id")
    suspend fun deleteRecordById(id: Long)
    
    @Query("DELETE FROM trade_record WHERE journalDate = :date")
    suspend fun deleteRecordsByDate(date: String)
}
