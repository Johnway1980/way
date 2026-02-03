package com.alphadoer.trader.domain.model.review

import com.squareup.moshi.JsonClass

/**
 * 改进计划
 */
@JsonClass(generateAdapter = true)
data class ImprovementPlan(
    val id: String,
    val date: String, // yyyy-MM-dd
    val relatedMistakes: List<String>,     // 关联的错误ID
    val actionItems: List<ActionItem>,     // 具体行动项
    val targetDate: String?,               // 目标完成日期
    val verificationMetrics: List<String>, // 验证指标
    val status: PlanStatus,                // 计划状态
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 行动项
     */
    @JsonClass(generateAdapter = true)
    data class ActionItem(
        val description: String,           // 行动描述
        val priority: Priority,            // 优先级
        val deadline: String?,            // 截止日期
        val status: ItemStatus,            // 状态
        val notes: String?                 // 备注
    )
    
    /**
     * 优先级
     */
    enum class Priority {
        HIGH,       // 高
        MEDIUM,     // 中
        LOW         // 低
    }
    
    /**
     * 行动项状态
     */
    enum class ItemStatus {
        PENDING,    // 待执行
        IN_PROGRESS, // 进行中
        COMPLETED,  // 已完成
        CANCELLED   // 已取消
    }
    
    /**
     * 计划状态
     */
    enum class PlanStatus {
        DRAFT,      // 草稿
        ACTIVE,     // 执行中
        COMPLETED,  // 已完成
        ARCHIVED    // 已归档
    }
}
