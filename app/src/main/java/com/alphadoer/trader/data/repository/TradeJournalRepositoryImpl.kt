package com.alphadoer.trader.data.repository

import com.alphadoer.trader.data.local.dao.TradeJournalDao
import com.alphadoer.trader.data.local.entity.TradeJournalEntity
import com.alphadoer.trader.domain.model.TradeJournal
import com.alphadoer.trader.domain.repository.TradeJournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 交易日报Repository实现
 */
class TradeJournalRepositoryImpl @Inject constructor(
    private val tradeJournalDao: TradeJournalDao
) : TradeJournalRepository {
    
    override fun getAllJournals(): Flow<List<TradeJournal>> =
        tradeJournalDao.getAllJournals().map { entities ->
            entities.map { it.toDomain() }
        }
    
    override suspend fun getJournalByDate(date: String): TradeJournal? =
        tradeJournalDao.getJournalByDate(date)?.toDomain()
    
    override fun getJournalByDateFlow(date: String): Flow<TradeJournal?> =
        tradeJournalDao.getJournalByDateFlow(date).map { it?.toDomain() }
    
    override suspend fun getJournalsByDateRange(startDate: String, endDate: String): List<TradeJournal> =
        tradeJournalDao.getJournalsByDateRange(startDate, endDate).map { it.toDomain() }
    
    override fun getJournalsByReviewStatus(completed: Boolean): Flow<List<TradeJournal>> =
        tradeJournalDao.getJournalsByReviewStatus(completed).map { entities ->
            entities.map { it.toDomain() }
        }
    
    override suspend fun insertJournal(journal: TradeJournal) {
        tradeJournalDao.insertJournal(journal.toEntity())
    }
    
    override suspend fun updateJournal(journal: TradeJournal) {
        val entity = journal.toEntity().copy(updatedAt = System.currentTimeMillis())
        tradeJournalDao.updateJournal(entity)
    }
    
    override suspend fun updateReviewStatus(date: String, completed: Boolean) {
        tradeJournalDao.updateReviewStatus(date, completed)
    }
    
    override suspend fun deleteJournal(date: String) {
        tradeJournalDao.deleteJournalByDate(date)
    }
    
    // ========== 数据转换 ==========
    private fun TradeJournalEntity.toDomain(): TradeJournal =
        TradeJournal(
            date = date,
            morningConclusion = morningConclusion,
            auctionFeeling = auctionFeeling,
            reviewCompleted = reviewCompleted
        )
    
    private fun TradeJournal.toEntity(): TradeJournalEntity =
        TradeJournalEntity.create(
            date = date,
            morningConclusion = morningConclusion,
            auctionFeeling = auctionFeeling,
            reviewCompleted = reviewCompleted
        )
}
