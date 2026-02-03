package com.alphadoer.trader.data.local.dao

import androidx.room.*
import com.alphadoer.trader.data.local.entity.TradeJournalEntity
import kotlinx.coroutines.flow.Flow

/**
 * 交易日报DAO
 */
@Dao
interface TradeJournalDao {
    
    @Query("SELECT * FROM trade_journal ORDER BY date DESC")
    fun getAllJournals(): Flow<List<TradeJournalEntity>>
    
    @Query("SELECT * FROM trade_journal WHERE date = :date")
    suspend fun getJournalByDate(date: String): TradeJournalEntity?
    
    @Query("SELECT * FROM trade_journal WHERE date = :date")
    fun getJournalByDateFlow(date: String): Flow<TradeJournalEntity?>
    
    @Query("SELECT * FROM trade_journal WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getJournalsByDateRange(startDate: String, endDate: String): List<TradeJournalEntity>
    
    @Query("SELECT * FROM trade_journal WHERE reviewCompleted = :completed ORDER BY date DESC")
    fun getJournalsByReviewStatus(completed: Boolean): Flow<List<TradeJournalEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: TradeJournalEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournals(journals: List<TradeJournalEntity>)
    
    @Update
    suspend fun updateJournal(journal: TradeJournalEntity)
    
    @Query("UPDATE trade_journal SET reviewCompleted = :completed, updatedAt = :timestamp WHERE date = :date")
    suspend fun updateReviewStatus(date: String, completed: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteJournal(journal: TradeJournalEntity)
    
    @Query("DELETE FROM trade_journal WHERE date = :date")
    suspend fun deleteJournalByDate(date: String)
    
    @Query("SELECT COUNT(*) FROM trade_journal")
    suspend fun getJournalCount(): Int
}
