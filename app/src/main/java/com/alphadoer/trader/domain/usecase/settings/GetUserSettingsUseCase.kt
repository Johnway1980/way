package com.alphadoer.trader.domain.usecase.settings

import com.alphadoer.trader.domain.model.settings.AppearanceSettings
import com.alphadoer.trader.domain.model.settings.DataSettings
import com.alphadoer.trader.domain.model.settings.FunctionalSettings
import com.alphadoer.trader.domain.model.settings.NotificationPreference
import com.alphadoer.trader.domain.model.settings.RiskControlConfig
import com.alphadoer.trader.domain.model.settings.TradingPreference
import com.alphadoer.trader.domain.model.settings.UserProfile
import com.alphadoer.trader.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * 获取用户所有设置用例
 */
class GetUserSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): UserSettings {
        return UserSettings(
            userProfile = settingsRepository.getUserProfile(),
            tradingPreference = settingsRepository.getTradingPreference() ?: getDefaultTradingPreference(),
            notificationPreference = settingsRepository.getNotificationPreference() ?: NotificationPreference(),
            appearanceSettings = settingsRepository.getAppearanceSettings() ?: getDefaultAppearanceSettings(),
            functionalSettings = settingsRepository.getFunctionalSettings() ?: getDefaultFunctionalSettings(),
            dataSettings = settingsRepository.getDataSettings() ?: DataSettings(),
            riskControlConfig = settingsRepository.getRiskControlConfig() ?: RiskControlConfig(
                maxSingleLoss = null,
                maxDailyLoss = null,
                maxPositionCount = null
            )
        )
    }
    
    private fun getDefaultTradingPreference(): TradingPreference {
        return TradingPreference(
            tradingStyle = TradingPreference.TradingStyle.SWING,
            riskLevel = TradingPreference.RiskLevel.MODERATE,
            positionStrategy = TradingPreference.PositionStrategy.DIVERSIFIED,
            defaultStopLoss = 5.0,
            defaultTakeProfit = 10.0,
            maxPositionCount = 10
        )
    }
    
    private fun getDefaultAppearanceSettings(): AppearanceSettings {
        return AppearanceSettings(
            themeMode = AppearanceSettings.ThemeMode.AUTO,
            fontSize = AppearanceSettings.FontSize.MEDIUM,
            animationEnabled = true
        )
    }

    private fun getDefaultFunctionalSettings(): FunctionalSettings {
        return FunctionalSettings(
            defaultStartPage = "home",
            autoSaveInterval = 30,
            gestureEnabled = true,
            hapticFeedback = true,
            showTutorial = true,
            aiEnabled = true,
            privacyModeEnabled = false
        )
    }
    
    /**
     * 用户所有设置的聚合类
     */
    data class UserSettings(
        val userProfile: UserProfile?,
        val tradingPreference: TradingPreference,
        val notificationPreference: NotificationPreference,
        val appearanceSettings: AppearanceSettings,
        val functionalSettings: FunctionalSettings,
        val dataSettings: DataSettings,
        val riskControlConfig: RiskControlConfig
    )
}
