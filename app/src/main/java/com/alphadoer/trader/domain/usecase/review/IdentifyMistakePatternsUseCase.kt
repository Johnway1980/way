package com.alphadoer.trader.domain.usecase.review

import com.alphadoer.trader.domain.model.review.MistakePattern
import com.alphadoer.trader.domain.model.review.TradeMistake
import com.alphadoer.trader.domain.repository.ReviewRepository
import javax.inject.Inject

/**
 * 识别错误模式用例
 */
class IdentifyMistakePatternsUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(dateRange: String? = null): Result<List<MistakePattern>> {
        return try {
            // 获取所有错误记录
            val allMistakes: List<TradeMistake> = if (dateRange != null) {
                // 按日期范围获取
                emptyList() // TODO: 实现按日期范围获取
            } else {
                // 获取所有错误（简化实现）
                emptyList()
            }
            
            // 按分类分组统计
            val patterns = groupMistakesByCategory(allMistakes)
            
            // 保存错误模式
            patterns.forEach { pattern ->
                reviewRepository.saveMistakePattern(pattern)
            }
            
            Result.success(patterns)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun groupMistakesByCategory(mistakes: List<TradeMistake>): List<MistakePattern> {
        val grouped = mistakes.groupBy { it.category }
        
        return grouped.map { (category, mistakeList) ->
            val totalLoss = mistakeList.sumOf { it.impactAmount }
            val averageLoss = if (mistakeList.isNotEmpty()) {
                totalLoss / mistakeList.size
            } else {
                0.0
            }
            
            MistakePattern(
                id = category.name,
                category = category,
                frequency = mistakeList.size,
                totalLoss = totalLoss,
                averageLoss = averageLoss,
                relatedMistakes = mistakeList.map { it.id },
                commonContext = mistakeList.firstOrNull()?.context?.marketEnvironment ?: "",
                improvementMeasures = emptyList(),
                effectiveness = null,
                lastOccurrence = mistakeList.maxOfOrNull { it.createdAt } ?: System.currentTimeMillis()
            )
        }
    }
}
