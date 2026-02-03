package com.alphadoer.trader.presentation.settings

import com.alphadoer.trader.domain.usecase.settings.GetUserSettingsUseCase

/**
 * 设置界面UI状态
 */
data class SettingsUiState(
    val userSettings: GetUserSettingsUseCase.UserSettings? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
