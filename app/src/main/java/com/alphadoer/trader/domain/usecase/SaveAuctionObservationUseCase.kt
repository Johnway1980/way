package com.alphadoer.trader.domain.usecase

import com.alphadoer.trader.domain.model.AuctionObservation
import com.alphadoer.trader.domain.model.TradeJournal
import com.alphadoer.trader.domain.repository.TradeJournalRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 保存集合竞价观察用例
 */
class SaveAuctionObservationUseCase @Inject constructor(
    private val tradeJournalRepository: TradeJournalRepository
) {
    suspend operator fun invoke(observation: AuctionObservation): Result<Unit> {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date())
            
            // 获取或创建今日交易日志
            var journal = tradeJournalRepository.getJournalByDate(today)
            if (journal == null) {
                journal = TradeJournal(
                    date = today,
                    morningConclusion = null,
                    auctionFeeling = observation.feeling,
                    reviewCompleted = false
                )
                tradeJournalRepository.insertJournal(journal)
            } else {
                // 更新集合竞价感受
                val updatedJournal = journal.copy(
                    auctionFeeling = observation.feeling
                )
                tradeJournalRepository.updateJournal(updatedJournal)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
