package com.alphadoer.trader.domain.model.process

/**
 * 步骤状态枚举
 */
enum class StepStatus {
    NOT_STARTED,    // 未开始
    IN_PROGRESS,    // 进行中
    COMPLETED,      // 已完成
    SKIPPED,        // 已跳过
    BLOCKED,        // 被阻塞（依赖未满足）
    EXPIRED         // 已过期（超出时间窗口）
}
