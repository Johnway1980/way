package com.alphadoer.trader.presentation.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphadoer.trader.domain.model.settings.AppearanceSettings
import com.alphadoer.trader.domain.usecase.settings.GetUserSettingsUseCase
import com.alphadoer.trader.domain.usecase.settings.UpdateAppearanceUseCase
import com.alphadoer.trader.domain.usecase.settings.UpdateFunctionalSettingsUseCase
import com.alphadoer.trader.domain.usecase.settings.UpdateNotificationPreferenceUseCase
import com.alphadoer.trader.presentation.settings.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val updateAppearanceUseCase: UpdateAppearanceUseCase,
    private val updateFunctionalSettingsUseCase: UpdateFunctionalSettingsUseCase,
    private val updateNotificationPreferenceUseCase: UpdateNotificationPreferenceUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val userSettings = getUserSettingsUseCase()
                _uiState.update {
                    it.copy(
                        userSettings = userSettings,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }
    
    fun updateAppearance(settings: AppearanceSettings) {
        viewModelScope.launch {
            updateAppearanceUseCase(settings)
                .onSuccess {
                    loadSettings() // 重新加载设置
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    /**
     * 更新AI分析校准参数，并立即应用到运行时的 StockValidationTuning。
     */
    fun updateAnalysisTuning(
        strictNullDomainMismatch: Boolean,
        minReasonLength: Int,
        samplingEnabled: Boolean,
        samplingRatio: Double
    ) {
        viewModelScope.launch {
            try {
                val current = _uiState.value.userSettings?.functionalSettings
                    ?: com.alphadoer.trader.domain.model.settings.FunctionalSettings()
                val updated = current.copy(
                    analysisStrictNullDomain = strictNullDomainMismatch,
                    analysisMinReasonLength = minReasonLength,
                    analysisSamplingEnabled = samplingEnabled,
                    analysisSamplingRatio = samplingRatio
                )
                updateFunctionalSettingsUseCase(updated)
                    .onSuccess {
                        // 应用到运行时校准配置
                        com.alphadoer.trader.data.util.StockValidationTuning.strictNullDomainMismatch = strictNullDomainMismatch
                        com.alphadoer.trader.data.util.StockValidationTuning.minReasonLength = minReasonLength
                        com.alphadoer.trader.data.util.StockValidationTuning.enableSamplingLog = samplingEnabled
                        com.alphadoer.trader.data.util.StockValidationTuning.samplingRatio = samplingRatio
                        // 重新加载设置
                        loadSettings()
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(errorMessage = e.message) }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 更新通知偏好（交易提醒/复盘提醒），保存后刷新设置。
     */
    fun updateNotificationPreference(pref: com.alphadoer.trader.domain.model.settings.NotificationPreference) {
        viewModelScope.launch {
            updateNotificationPreferenceUseCase(pref)
                .onSuccess { loadSettings() }
                .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
        }
    }
}
