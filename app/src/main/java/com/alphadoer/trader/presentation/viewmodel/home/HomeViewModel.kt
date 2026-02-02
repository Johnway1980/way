package com.alphadoer.trader.presentation.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphadoer.trader.domain.model.process.DailyProcess
import com.alphadoer.trader.domain.model.process.ProcessStep
import com.alphadoer.trader.domain.usecase.process.ProcessManager
import com.alphadoer.trader.presentation.home.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val processManager: ProcessManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadTodayProcess()
    }
    
    fun loadTodayProcess() {
        viewModelScope.launch {
            processManager.getTodayProcess()
                .catch { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
                .collect { process ->
                    if (process != null) {
                        _uiState.update { it.copy(process = process) }
                    } else {
                        // 初始化流程
                        initializeProcess()
                    }
                }
        }
    }
    
    private fun initializeProcess() {
        viewModelScope.launch {
            processManager.initializeTodayProcess()
                .onSuccess { process ->
                    _uiState.update { it.copy(process = process) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }
    
    fun startStep(stepId: String) {
        viewModelScope.launch {
            processManager.startStep(stepId)
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }
    
    fun getNextStep() {
        viewModelScope.launch {
            val nextStep = processManager.getNextStep()
            _uiState.update { it.copy(nextStep = nextStep) }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
