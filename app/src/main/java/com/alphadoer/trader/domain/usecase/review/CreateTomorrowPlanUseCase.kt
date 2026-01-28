package com.alphadoer.trader.domain.usecase.review

import com.alphadoer.trader.domain.model.review.ImprovementPlan
import com.alphadoer.trader.domain.repository.ReviewRepository
import javax.inject.Inject

/**
 * 创建次日交易计划用例
 */
class CreateTomorrowPlanUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(
        date: String,
        focusSectors: List<String>,
        focusStocks: List<String>,
        strategies: List<String>
    ): Result<ImprovementPlan> {
        return try {
            val actionItems = mutableListOf<ImprovementPlan.ActionItem>()
            
            // 添加关注板块
            focusSectors.forEach { sector ->
                actionItems.add(
                    ImprovementPlan.ActionItem(
                        description = "关注板块：$sector",
                        priority = ImprovementPlan.Priority.MEDIUM,
                        deadline = null,
                        status = ImprovementPlan.ItemStatus.PENDING,
                        notes = null
                    )
                )
            }
            
            // 添加关注个股
            focusStocks.forEach { stock ->
                actionItems.add(
                    ImprovementPlan.ActionItem(
                        description = "关注个股：$stock",
                        priority = ImprovementPlan.Priority.HIGH,
                        deadline = null,
                        status = ImprovementPlan.ItemStatus.PENDING,
                        notes = null
                    )
                )
            }
            
            // 添加交易策略
            strategies.forEach { strategy ->
                actionItems.add(
                    ImprovementPlan.ActionItem(
                        description = "交易策略：$strategy",
                        priority = ImprovementPlan.Priority.HIGH,
                        deadline = null,
                        status = ImprovementPlan.ItemStatus.PENDING,
                        notes = null
                    )
                )
            }
            
            val plan = ImprovementPlan(
                id = "tomorrow_plan_$date",
                date = date,
                relatedMistakes = emptyList(),
                actionItems = actionItems,
                targetDate = date,
                verificationMetrics = listOf("执行率", "胜率", "盈亏比"),
                status = ImprovementPlan.PlanStatus.ACTIVE
            )
            
            reviewRepository.saveImprovementPlan(plan)
            Result.success(plan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
