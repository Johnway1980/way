package com.alphadoer.trader.domain.usecase

import com.alphadoer.trader.domain.model.PreMarketPlan
import com.alphadoer.trader.domain.model.TradeJournal
import com.alphadoer.trader.domain.repository.TradeJournalRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * 保存盘前计划用例
 */
class SavePreMarketPlanUseCase @Inject constructor(
    private val tradeJournalRepository: TradeJournalRepository
) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    
    suspend operator fun invoke(plan: PreMarketPlan): Result<Unit> {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date())
            
            // 将计划转换为JSON保存到TradeJournal
            val planJson = moshi.adapter(PreMarketPlan::class.java).toJson(plan)
            
            // 获取或创建今日交易日志
            var journal = tradeJournalRepository.getJournalByDate(today)
            if (journal == null) {
                journal = TradeJournal(
                    date = today,
                    morningConclusion = null,
                    auctionFeeling = null,
                    reviewCompleted = false
                )
                tradeJournalRepository.insertJournal(journal)
            }
            
            // 更新交易日志（将计划保存到morningConclusion字段，实际应该用专门字段）
            // 这里暂时保存，后续可以扩展TradeJournal实体
            val updatedJournal = journal.copy(
                morningConclusion = planJson // 临时方案，后续应扩展实体
            )
            tradeJournalRepository.updateJournal(updatedJournal)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
