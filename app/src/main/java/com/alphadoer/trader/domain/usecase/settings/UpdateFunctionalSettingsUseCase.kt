package com.alphadoer.trader.domain.usecase.settings

import com.alphadoer.trader.domain.model.settings.FunctionalSettings
import com.alphadoer.trader.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * 更新功能设置用例（包含AI分析校准参数）。
 */
class UpdateFunctionalSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(settings: FunctionalSettings): Result<Unit> {
        return settingsRepository.saveFunctionalSettings(settings)
    }
}
