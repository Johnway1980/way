package com.alphadoer.trader.domain.model.process

import com.squareup.moshi.JsonClass

/**
 * 每日流程数据类
 */
@JsonClass(generateAdapter = true)
data class DailyProcess(
    val id: String,
    val date: String, // yyyy-MM-dd格式
    val steps: List<ProcessStep>,
    val stepStates: Map<String, StepState>, // stepId -> StepState
    val overallProgress: Double = 0.0, // 0.0-1.0
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 获取步骤状态
     */
    fun getStepState(stepId: String): StepState? = stepStates[stepId]
    
    /**
     * 计算总体进度
     */
    fun calculateProgress(): Double {
        if (steps.isEmpty()) return 0.0
        val completedCount = stepStates.values.count { 
            it.status == StepStatus.COMPLETED 
        }
        return completedCount.toDouble() / steps.size
    }
    
    /**
     * 获取下一个可用步骤
     */
    fun getNextAvailableStep(): ProcessStep? {
        return steps.firstOrNull { step ->
            val state = stepStates[step.id]
            val isAvailable = when (state?.status) {
                null, StepStatus.NOT_STARTED, StepStatus.BLOCKED -> {
                    // 检查依赖是否满足
                    step.dependencies.all { depId ->
                        stepStates[depId]?.status == StepStatus.COMPLETED
                    }
                }
                else -> false
            }
            isAvailable
        }
    }
}
