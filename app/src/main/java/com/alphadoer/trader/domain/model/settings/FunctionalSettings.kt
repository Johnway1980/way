package com.alphadoer.trader.domain.model.settings

import com.squareup.moshi.JsonClass

/**
 * 功能设置
 */
@JsonClass(generateAdapter = true)
data class FunctionalSettings(
    val defaultStartPage: String = "home", // 默认启动页
    val autoSaveInterval: Int = 30, // 自动保存间隔（秒）
    val gestureEnabled: Boolean = true, // 手势操作
    val hapticFeedback: Boolean = true, // 触觉反馈
    val showTutorial: Boolean = true, // 显示教程提示
    val aiEnabled: Boolean = true, // 是否启用AI功能
    val privacyModeEnabled: Boolean = false, // 隐私模式（最小化数据存储与上传）
    // ==== AI分析规则校准参数（可通过设置页调整）====
    val analysisStrictNullDomain: Boolean = false, // 严格模式：为空领域视为不匹配
    val analysisMinReasonLength: Int = 10, // 推荐理由最小长度
    val analysisSamplingEnabled: Boolean = true, // 是否启用采样日志
    val analysisSamplingRatio: Double = 0.1, // 采样比例 0.0~1.0
    val updatedAt: Long = System.currentTimeMillis()
)
