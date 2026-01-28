package com.alphadoer.trader.domain.usecase.settings

import com.alphadoer.trader.domain.model.settings.AppearanceSettings
import com.alphadoer.trader.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * 更新外观设置用例
 */
class UpdateAppearanceUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(settings: AppearanceSettings): Result<Unit> {
        return settingsRepository.saveAppearanceSettings(settings)
    }
}
