package com.alphadoer.trader.domain.model.process

import com.squareup.moshi.JsonClass

/**
 * 步骤状态数据类
 */
@JsonClass(generateAdapter = true)
data class StepState(
    val stepId: String,
    val status: StepStatus,
    val startedAt: Long? = null, // 开始时间戳
    val completedAt: Long? = null, // 完成时间戳
    val skippedAt: Long? = null, // 跳过时间戳
    val actualDuration: Long? = null, // 实际耗时（分钟）
    val notes: String? = null, // 备注
    val data: Map<String, String>? = null // 步骤相关数据
)
