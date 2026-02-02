package com.alphadoer.trader.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.alphadoer.trader.domain.model.settings.AppearanceSettings
import com.alphadoer.trader.domain.model.settings.DataSettings
import com.alphadoer.trader.domain.model.settings.FunctionalSettings
import com.alphadoer.trader.domain.model.settings.NotificationPreference
import com.alphadoer.trader.domain.model.settings.RiskControlConfig
import com.alphadoer.trader.domain.model.settings.TradingPreference
import com.alphadoer.trader.domain.model.settings.UserProfile
import com.alphadoer.trader.domain.repository.SettingsRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 设置Repository实现（使用SharedPreferences存储）
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "alpha_doer_settings",
        Context.MODE_PRIVATE
    )
    
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    
    private val appearanceSettingsFlow = MutableStateFlow<AppearanceSettings?>(null)
    
    // ========== 用户资料 ==========
    override suspend fun getUserProfile(): UserProfile? {
        val json = prefs.getString("user_profile", null) ?: return null
        return moshi.adapter(UserProfile::class.java).fromJson(json)
    }
    
    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val json = moshi.adapter(UserProfile::class.java).toJson(profile)
            prefs.edit().putString("user_profile", json).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 交易偏好 ==========
    override suspend fun getTradingPreference(): TradingPreference? {
        val json = prefs.getString("trading_preference", null) ?: return null
        return moshi.adapter(TradingPreference::class.java).fromJson(json)
    }
    
    override suspend fun saveTradingPreference(preference: TradingPreference): Result<Unit> {
        return try {
            val json = moshi.adapter(TradingPreference::class.java).toJson(preference)
            prefs.edit().putString("trading_preference", json).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 通知偏好 ==========
    override suspend fun getNotificationPreference(): NotificationPreference? {
        val json = prefs.getString("notification_preference", null) ?: return null
        return moshi.adapter(NotificationPreference::class.java).fromJson(json)
    }
    
    override suspend fun saveNotificationPreference(preference: NotificationPreference): Result<Unit> {
        return try {
            val json = moshi.adapter(NotificationPreference::class.java).toJson(preference)
            prefs.edit().putString("notification_preference", json).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 外观设置 ==========
    override suspend fun getAppearanceSettings(): AppearanceSettings? {
        val json = prefs.getString("appearance_settings", null) ?: return null
        val settings = moshi.adapter(AppearanceSettings::class.java).fromJson(json)
        // 更新Flow值
        appearanceSettingsFlow.value = settings
        return settings
    }
    
    override suspend fun saveAppearanceSettings(settings: AppearanceSettings): Result<Unit> {
        return try {
            val json = moshi.adapter(AppearanceSettings::class.java).toJson(settings)
            prefs.edit().putString("appearance_settings", json).apply()
            appearanceSettingsFlow.value = settings
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun observeAppearanceSettings(): Flow<AppearanceSettings> {
        return kotlinx.coroutines.flow.flow {
            // 首次加载（如果还没有加载）
            val current = appearanceSettingsFlow.value
            if (current != null) {
                emit(current)
            }
            // 观察后续变化
            appearanceSettingsFlow.collect { settings ->
                settings?.let { emit(it) }
            }
        }
    }
    
    // ========== 功能设置 ==========
    override suspend fun getFunctionalSettings(): FunctionalSettings? {
        val json = prefs.getString("functional_settings", null) ?: return null
        return moshi.adapter(FunctionalSettings::class.java).fromJson(json)
    }
    
    override suspend fun saveFunctionalSettings(settings: FunctionalSettings): Result<Unit> {
        return try {
            val json = moshi.adapter(FunctionalSettings::class.java).toJson(settings)
            prefs.edit().putString("functional_settings", json).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 数据设置 ==========
    override suspend fun getDataSettings(): DataSettings? {
        val json = prefs.getString("data_settings", null) ?: return null
        return moshi.adapter(DataSettings::class.java).fromJson(json)
    }
    
    override suspend fun saveDataSettings(settings: DataSettings): Result<Unit> {
        return try {
            val json = moshi.adapter(DataSettings::class.java).toJson(settings)
            prefs.edit().putString("data_settings", json).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 风险控制 ==========
    override suspend fun getRiskControlConfig(): RiskControlConfig? {
        val json = prefs.getString("risk_control_config", null) ?: return null
        return moshi.adapter(RiskControlConfig::class.java).fromJson(json)
    }
    
    override suspend fun saveRiskControlConfig(config: RiskControlConfig): Result<Unit> {
        return try {
            val json = moshi.adapter(RiskControlConfig::class.java).toJson(config)
            prefs.edit().putString("risk_control_config", json).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 通用操作 ==========
    override suspend fun resetToDefaults(): Result<Unit> {
        return try {
            prefs.edit().clear().apply()
            appearanceSettingsFlow.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun exportSettings(): Result<String> {
        return try {
            val export = com.alphadoer.trader.domain.model.settings.AllSettingsExport(
                userProfile = getUserProfile(),
                tradingPreference = getTradingPreference(),
                notificationPreference = getNotificationPreference(),
                appearanceSettings = getAppearanceSettings(),
                functionalSettings = getFunctionalSettings(),
                dataSettings = getDataSettings(),
                riskControlConfig = getRiskControlConfig()
            )
            val json = moshi.adapter(com.alphadoer.trader.domain.model.settings.AllSettingsExport::class.java).toJson(export)
            Result.success(json)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun importSettings(json: String): Result<Unit> {
        return try {
            val export = moshi.adapter(com.alphadoer.trader.domain.model.settings.AllSettingsExport::class.java)
                .fromJson(json) ?: return Result.failure(IllegalArgumentException("JSON为空或格式错误"))
            export.userProfile?.let { saveUserProfile(it) }
            export.tradingPreference?.let { saveTradingPreference(it) }
            export.notificationPreference?.let { saveNotificationPreference(it) }
            export.appearanceSettings?.let { saveAppearanceSettings(it) }
            export.functionalSettings?.let { saveFunctionalSettings(it) }
            export.dataSettings?.let { saveDataSettings(it) }
            export.riskControlConfig?.let { saveRiskControlConfig(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
