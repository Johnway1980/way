package com.alphadoer.trader.domain.model.settings

import com.squareup.moshi.JsonClass

/**
 * 设置导出/导入的聚合结构
 */
@JsonClass(generateAdapter = true)
data class AllSettingsExport(
    val userProfile: UserProfile?,
    val tradingPreference: TradingPreference?,
    val notificationPreference: NotificationPreference?,
    val appearanceSettings: AppearanceSettings?,
    val functionalSettings: FunctionalSettings?,
    val dataSettings: DataSettings?,
    val riskControlConfig: RiskControlConfig?
)
