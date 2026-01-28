package com.alphadoer.trader.domain.usecase.review

import com.alphadoer.trader.domain.model.review.DailyReviewSummary
import com.alphadoer.trader.domain.model.review.ImprovementPlan
import com.alphadoer.trader.domain.model.review.TradeMistake
import com.alphadoer.trader.domain.model.trading.TradeStatistics
import com.alphadoer.trader.domain.repository.ReviewRepository
import com.alphadoer.trader.domain.repository.TradeRecordRepository
import javax.inject.Inject

/**
 * 生成当日总结用例
 */
class GenerateDailySummaryUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val tradeRecordRepository: TradeRecordRepository
) {
    suspend operator fun invoke(date: String): Result<DailyReviewSummary> {
        return try {
            // 获取当日交易统计
            val statistics = tradeRecordRepository.getTradeStatistics(date)
            
            // 获取当日错误
            val mistakes = reviewRepository.getTradeMistakesByDate(date)
            
            // 生成成功经验
            val successExperiences = generateSuccessExperiences(statistics)
            
            // 生成失败教训
            val failureLessons = generateFailureLessons(mistakes)
            
            // 生成关键观察
            val keyObservations = generateKeyObservations(statistics, mistakes)
            
            // 生成改进计划
            val improvementPlan = generateImprovementPlan(mistakes, date)
            
            val summary = DailyReviewSummary(
                date = date,
                successExperiences = successExperiences,
                failureLessons = failureLessons,
                marketInsights = null,
                keyObservations = keyObservations,
                tomorrowPlan = improvementPlan,
                overallRating = generateOverallRating(statistics, mistakes)
            )
            
            reviewRepository.saveDailyReviewSummary(summary)
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateSuccessExperiences(statistics: TradeStatistics): List<String> {
        val experiences = mutableListOf<String>()
        
        if (statistics.winRate > 60) {
            experiences.add("今日胜率较高（${String.format("%.1f", statistics.winRate)}%），交易策略有效")
        }
        
        if (statistics.totalProfitLoss > 0) {
            experiences.add("今日实现盈利，总盈亏：${String.format("%.2f", statistics.totalProfitLoss)}元")
        }
        
        return experiences
    }
    
    private fun generateFailureLessons(mistakes: List<TradeMistake>): List<String> {
        return mistakes.map { mistake ->
            "${mistake.category.name}: ${mistake.description}"
        }
    }
    
    private fun generateKeyObservations(
        statistics: TradeStatistics,
        mistakes: List<TradeMistake>
    ): List<String> {
        val observations = mutableListOf<String>()
        
        if (statistics.totalTrades > 10) {
            observations.add("今日交易次数较多（${statistics.totalTrades}次），需注意过度交易")
        }
        
        if (mistakes.isNotEmpty()) {
            observations.add("发现${mistakes.size}个交易错误，需要重点关注")
        }
        
        return observations
    }
    
    private suspend fun generateImprovementPlan(
        mistakes: List<TradeMistake>,
        date: String
    ): ImprovementPlan? {
        if (mistakes.isEmpty()) {
            return null
        }
        
        val actionItems = mistakes.map { mistake ->
            ImprovementPlan.ActionItem(
                description = "改进${mistake.category.name}：${mistake.description}",
                priority = ImprovementPlan.Priority.HIGH,
                deadline = null,
                status = ImprovementPlan.ItemStatus.PENDING,
                notes = null
            )
        }
        
        return ImprovementPlan(
            id = "plan_$date",
            date = date,
            relatedMistakes = mistakes.map { it.id },
            actionItems = actionItems,
            targetDate = null,
            verificationMetrics = listOf("错误重复率降低", "交易胜率提升"),
            status = ImprovementPlan.PlanStatus.ACTIVE
        )
    }
    
    private fun generateOverallRating(
        statistics: TradeStatistics,
        mistakes: List<TradeMistake>
    ): String {
        val score = when {
            statistics.totalProfitLoss > 0 && mistakes.isEmpty() -> "优秀"
            statistics.totalProfitLoss > 0 && mistakes.size <= 2 -> "良好"
            statistics.totalProfitLoss > 0 -> "一般"
            mistakes.size > 5 -> "需改进"
            else -> "一般"
        }
        return score
    }
}
