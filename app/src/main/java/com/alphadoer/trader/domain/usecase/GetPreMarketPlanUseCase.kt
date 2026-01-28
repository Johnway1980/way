package com.alphadoer.trader.domain.usecase

import com.alphadoer.trader.domain.model.PreMarketPlan
import com.alphadoer.trader.domain.repository.TradeJournalRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 获取盘前计划用例
 */
class GetPreMarketPlanUseCase @Inject constructor(
    private val tradeJournalRepository: TradeJournalRepository
) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    
    suspend operator fun invoke(date: String): PreMarketPlan? {
        return try {
            val journal = tradeJournalRepository.getJournalByDate(date)
            journal?.morningConclusion?.let { json ->
                try {
                    moshi.adapter(PreMarketPlan::class.java).fromJson(json)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
