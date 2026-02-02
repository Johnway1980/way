package com.alphadoer.trader.domain.usecase.trading

import com.alphadoer.trader.domain.model.trading.TradeRecord
import com.alphadoer.trader.domain.repository.TradeRecordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 按日期获取交易记录用例
 */
class GetTradesByDateUseCase @Inject constructor(
    private val tradeRecordRepository: TradeRecordRepository
) {
    operator fun invoke(date: String): Flow<List<TradeRecord>> {
        return tradeRecordRepository.getTradesByDate(date)
    }
}
