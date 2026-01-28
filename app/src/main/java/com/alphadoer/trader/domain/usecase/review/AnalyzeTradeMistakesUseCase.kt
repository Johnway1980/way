package com.alphadoer.trader.domain.usecase.review

import com.alphadoer.trader.domain.model.review.TradeMistake
import com.alphadoer.trader.domain.model.trading.TradeRecord
import com.alphadoer.trader.domain.repository.ReviewRepository
import com.alphadoer.trader.domain.repository.TradeRecordRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 分析交易错误用例
 */
class AnalyzeTradeMistakesUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val tradeRecordRepository: TradeRecordRepository
) {
    suspend operator fun invoke(date: String): Result<List<TradeMistake>> {
        return try {
            // 获取当日交易记录
            val tradesFlow = tradeRecordRepository.getTradesByDate(date)
            val trades = tradesFlow.first()
            
            // 分析每笔交易，识别潜在错误
            val mistakes = trades.mapNotNull { trade ->
                analyzeTradeForMistakes(trade, date)
            }
            
            // 保存错误记录
            mistakes.forEach { mistake ->
                reviewRepository.saveTradeMistake(mistake)
            }
            
            Result.success(mistakes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun analyzeTradeForMistakes(
        trade: TradeRecord,
        date: String
    ): TradeMistake? {
        // 简单的错误识别逻辑（实际应更复杂）
        val mistakes = mutableListOf<String>()
        var mistakeType: TradeMistake.MistakeType? = null
        var category: TradeMistake.MistakeCategory? = null
        
        // 检查是否有亏损
        if (trade.profitLoss != null && trade.profitLoss!! < 0) {
            mistakes.add("交易产生亏损")
            
            // 根据交易特征判断错误类型
            if (trade.isBuy() && trade.price > 0) {
                // 买入后亏损，可能是追高
                category = TradeMistake.MistakeCategory.CHASING_HIGH
                mistakeType = TradeMistake.MistakeType.EXECUTION
            }
        }
        
        // 检查是否有止损
        if (trade.stopLoss == null && trade.isBuy()) {
            mistakes.add("买入未设置止损")
            if (category == null) {
                category = TradeMistake.MistakeCategory.NO_STOP_LOSS
                mistakeType = TradeMistake.MistakeType.DISCIPLINE
            }
        }
        
        if (mistakes.isEmpty()) {
            return null
        }
        
        return TradeMistake(
            tradeRecordId = trade.id,
            date = date,
            mistakeType = mistakeType ?: TradeMistake.MistakeType.COGNITIVE,
            category = category ?: TradeMistake.MistakeCategory.ANALYSIS_ERROR,
            description = mistakes.joinToString("; "),
            rootCause = "需要进一步分析",
            impactAmount = trade.profitLoss ?: 0.0,
            context = TradeMistake.MistakeContext(
                marketEnvironment = "当日市场",
                emotionState = trade.emotion?.name ?: "未知",
                timeOfDay = formatTime(trade.timestamp),
                stockCode = trade.stockCode,
                notes = trade.notes
            ),
            improvementMeasures = emptyList(),
            relatedMistakes = emptyList()
        )
    }
    
    private fun formatTime(timestamp: Long): String {
        val hour = (timestamp / (1000 * 60 * 60)) % 24
        return when {
            hour in 9..11 -> "早盘"
            hour in 13..15 -> "午盘"
            else -> "其他时段"
        }
    }
}
