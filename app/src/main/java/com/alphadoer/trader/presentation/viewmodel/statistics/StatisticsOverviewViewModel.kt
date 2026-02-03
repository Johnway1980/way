package com.alphadoer.trader.presentation.viewmodel.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphadoer.trader.domain.model.statistics.TradingPerformance
import com.alphadoer.trader.domain.usecase.statistics.CalculatePerformanceMetricsUseCase
import com.alphadoer.trader.presentation.statistics.StatisticsOverviewUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class StatisticsOverviewViewModel @Inject constructor(
    private val calculatePerformanceMetricsUseCase: CalculatePerformanceMetricsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StatisticsOverviewUiState())
    val uiState: StateFlow<StatisticsOverviewUiState> = _uiState.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    fun loadPerformanceMetrics(timeRange: TimeRange = TimeRange.ALL) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val (startDate, endDate) = getDateRange(timeRange)
            
            calculatePerformanceMetricsUseCase(startDate, endDate)
                .onSuccess { performance ->
                    _uiState.update { 
                        it.copy(
                            performance = performance,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                }
        }
    }
    
    private fun getDateRange(timeRange: TimeRange): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val endDate = dateFormat.format(calendar.time)
        
        when (timeRange) {
            TimeRange.MONTH_1 -> {
                calendar.add(Calendar.MONTH, -1)
            }
            TimeRange.MONTH_3 -> {
                calendar.add(Calendar.MONTH, -3)
            }
            TimeRange.MONTH_6 -> {
                calendar.add(Calendar.MONTH, -6)
            }
            TimeRange.YEAR_1 -> {
                calendar.add(Calendar.YEAR, -1)
            }
            TimeRange.ALL -> {
                calendar.set(2020, 0, 1) // 设置一个较早的起始日期
            }
        }
        
        val startDate = dateFormat.format(calendar.time)
        return Pair(startDate, endDate)
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    enum class TimeRange {
        MONTH_1, MONTH_3, MONTH_6, YEAR_1, ALL
    }
}
