package com.alphadoer.trader.domain.usecase.settings

import com.alphadoer.trader.domain.model.settings.NotificationPreference
import com.alphadoer.trader.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * 更新通知偏好用例。
 */
class UpdateNotificationPreferenceUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(pref: NotificationPreference): Result<Unit> {
        return settingsRepository.saveNotificationPreference(pref)
    }
}
