package com.alphadoer.trader.domain.usecase.trading

import com.alphadoer.trader.domain.model.trading.TradeRecord
import com.alphadoer.trader.domain.repository.TradeRecordRepository
import javax.inject.Inject

/**
 * 更新交易用例
 */
class UpdateTradeUseCase @Inject constructor(
    private val tradeRecordRepository: TradeRecordRepository
) {
    suspend operator fun invoke(record: TradeRecord): Result<Unit> {
        // 验证交易记录
        if (record.id.isBlank()) {
            return Result.failure(IllegalArgumentException("交易记录ID不能为空"))
        }
        
        // 计算成交金额
        val calculatedAmount = record.calculateAmount()
        val updatedRecord = record.copy(
            amount = calculatedAmount,
            updatedAt = System.currentTimeMillis()
        )
        
        // 更新交易记录
        return tradeRecordRepository.updateTradeRecord(updatedRecord)
    }
}
