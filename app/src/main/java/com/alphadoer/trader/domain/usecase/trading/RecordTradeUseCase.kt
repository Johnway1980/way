package com.alphadoer.trader.domain.usecase.trading

import com.alphadoer.trader.domain.model.trading.TradeRecord
import com.alphadoer.trader.domain.repository.TradeRecordRepository
import javax.inject.Inject

/**
 * 记录交易用例
 */
class RecordTradeUseCase @Inject constructor(
    private val tradeRecordRepository: TradeRecordRepository
) {
    suspend operator fun invoke(record: TradeRecord): Result<Unit> {
        // 验证交易记录
        val validationResult = validateTradeRecord(record)
        if (validationResult.isFailure) {
            return validationResult
        }
        
        // 计算成交金额
        val calculatedAmount = record.calculateAmount()
        val updatedRecord = record.copy(amount = calculatedAmount)
        
        // 保存交易记录
        return tradeRecordRepository.saveTradeRecord(updatedRecord)
    }
    
    private fun validateTradeRecord(record: TradeRecord): Result<Unit> {
        if (record.stockCode.isBlank()) {
            return Result.failure(IllegalArgumentException("股票代码不能为空"))
        }
        if (record.price <= 0) {
            return Result.failure(IllegalArgumentException("价格必须大于0"))
        }
        if (record.quantity <= 0) {
            return Result.failure(IllegalArgumentException("数量必须大于0"))
        }
        return Result.success(Unit)
    }
}
