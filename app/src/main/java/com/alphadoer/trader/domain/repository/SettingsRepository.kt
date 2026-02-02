package com.alphadoer.trader.domain.repository

import com.alphadoer.trader.domain.model.settings.AppearanceSettings
import com.alphadoer.trader.domain.model.settings.DataSettings
import com.alphadoer.trader.domain.model.settings.FunctionalSettings
import com.alphadoer.trader.domain.model.settings.NotificationPreference
import com.alphadoer.trader.domain.model.settings.RiskControlConfig
import com.alphadoer.trader.domain.model.settings.TradingPreference
import com.alphadoer.trader.domain.model.settings.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * 设置Repository接口
 */
interface SettingsRepository {
    
    // ========== 用户资料 ==========
    suspend fun getUserProfile(): UserProfile?
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit>
    
    // ========== 交易偏好 ==========
    suspend fun getTradingPreference(): TradingPreference?
    suspend fun saveTradingPreference(preference: TradingPreference): Result<Unit>
    
    // ========== 通知偏好 ==========
    suspend fun getNotificationPreference(): NotificationPreference?
    suspend fun saveNotificationPreference(preference: NotificationPreference): Result<Unit>
    
    // ========== 外观设置 ==========
    suspend fun getAppearanceSettings(): AppearanceSettings?
    suspend fun saveAppearanceSettings(settings: AppearanceSettings): Result<Unit>
    fun observeAppearanceSettings(): Flow<AppearanceSettings>
    
    // ========== 功能设置 ==========
    suspend fun getFunctionalSettings(): FunctionalSettings?
    suspend fun saveFunctionalSettings(settings: FunctionalSettings): Result<Unit>
    
    // ========== 数据设置 ==========
    suspend fun getDataSettings(): DataSettings?
    suspend fun saveDataSettings(settings: DataSettings): Result<Unit>
    
    // ========== 风险控制 ==========
    suspend fun getRiskControlConfig(): RiskControlConfig?
    suspend fun saveRiskControlConfig(config: RiskControlConfig): Result<Unit>
    
    // ========== 通用操作 ==========
    suspend fun resetToDefaults(): Result<Unit>
    suspend fun exportSettings(): Result<String> // 返回JSON字符串
    suspend fun importSettings(json: String): Result<Unit>
}
