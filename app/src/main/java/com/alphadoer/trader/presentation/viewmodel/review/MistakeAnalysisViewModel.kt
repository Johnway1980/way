package com.alphadoer.trader.presentation.viewmodel.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphadoer.trader.domain.model.review.MistakePattern
import com.alphadoer.trader.domain.model.review.MistakeStatistics
import com.alphadoer.trader.domain.model.review.TradeMistake
import com.alphadoer.trader.domain.model.trading.TradeRecord
import com.alphadoer.trader.domain.usecase.review.AnalyzeTradeMistakesUseCase
import com.alphadoer.trader.domain.usecase.review.IdentifyMistakePatternsUseCase
import com.alphadoer.trader.domain.usecase.trading.GetTradesByDateUseCase
import com.alphadoer.trader.presentation.review.MistakeAnalysisUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MistakeAnalysisViewModel @Inject constructor(
    private val analyzeTradeMistakesUseCase: AnalyzeTradeMistakesUseCase,
    private val identifyMistakePatternsUseCase: IdentifyMistakePatternsUseCase,
    private val getTradesByDateUseCase: GetTradesByDateUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MistakeAnalysisUiState())
    val uiState: StateFlow<MistakeAnalysisUiState> = _uiState.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    fun loadTradesAndAnalyze(date: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // 加载交易记录
            getTradesByDateUseCase(date)
                .catch { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
                .collect { trades ->
                    _uiState.update { it.copy(trades = trades) }
                    
                    // 分析错误
                    analyzeMistakes(date)
                }
        }
    }
    
    private fun analyzeMistakes(date: String) {
        viewModelScope.launch {
            analyzeTradeMistakesUseCase(date)
                .onSuccess { mistakes ->
                    _uiState.update { it.copy(mistakes = mistakes) }
                    
                    // 识别错误模式
                    identifyPatterns()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }
    
    private fun identifyPatterns() {
        viewModelScope.launch {
            identifyMistakePatternsUseCase()
                .onSuccess { patterns ->
                    _uiState.update { it.copy(mistakePatterns = patterns) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }
    
    fun selectTrade(trade: TradeRecord) {
        _uiState.update { it.copy(selectedTrade = trade) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
