package com.alphadoer.trader.domain.repository

import com.alphadoer.trader.domain.model.TradeJournal
import kotlinx.coroutines.flow.Flow

/**
 * 交易日报Repository接口
 */
interface TradeJournalRepository {
    
    fun getAllJournals(): Flow<List<TradeJournal>>
    
    suspend fun getJournalByDate(date: String): TradeJournal?
    
    fun getJournalByDateFlow(date: String): Flow<TradeJournal?>
    
    suspend fun getJournalsByDateRange(startDate: String, endDate: String): List<TradeJournal>
    
    fun getJournalsByReviewStatus(completed: Boolean): Flow<List<TradeJournal>>
    
    suspend fun insertJournal(journal: TradeJournal)
    
    suspend fun updateJournal(journal: TradeJournal)
    
    suspend fun updateReviewStatus(date: String, completed: Boolean)
    
    suspend fun deleteJournal(date: String)
}
