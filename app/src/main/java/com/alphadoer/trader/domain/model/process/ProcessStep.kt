package com.alphadoer.trader.domain.model.process

import com.squareup.moshi.JsonClass

/**
 * 流程步骤数据类
 */
@JsonClass(generateAdapter = true)
data class ProcessStep(
    val id: String,
    val type: StepType,
    val name: String,
    val description: String,
    val icon: String? = null, // 图标资源名或URL
    val estimatedDuration: Long = 0, // 预计耗时（分钟）
    val dependencies: List<String> = emptyList(), // 依赖的步骤ID列表
    val timeWindow: TimeWindow? = null, // 时间窗口
    val route: String, // 对应的导航路由
    val required: Boolean = true, // 是否必需步骤
    val order: Int = 0 // 显示顺序
) {
    /**
     * 时间窗口
     */
    @JsonClass(generateAdapter = true)
    data class TimeWindow(
        val startTime: String, // HH:mm格式
        val endTime: String,   // HH:mm格式
        val timezone: String = "Asia/Shanghai"
    )
}
