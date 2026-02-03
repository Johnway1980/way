package com.alphadoer.trader.presentation.viewmodel.trading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphadoer.trader.domain.model.RecommendedStock
import com.alphadoer.trader.domain.model.trading.TradeOperation
import com.alphadoer.trader.domain.model.trading.TradeRecord
import com.alphadoer.trader.domain.model.trading.TradeStatistics
import com.alphadoer.trader.domain.repository.NewsAnalysisRepository
import com.alphadoer.trader.domain.repository.StockRepository
import com.alphadoer.trader.domain.usecase.process.ProcessManager
import com.alphadoer.trader.domain.usecase.trading.CalculateProfitLossUseCase
import com.alphadoer.trader.domain.usecase.trading.GetTradesByDateUseCase
import com.alphadoer.trader.domain.usecase.trading.GetTradingStatisticsUseCase
import com.alphadoer.trader.domain.usecase.trading.RecordTradeUseCase
import com.alphadoer.trader.domain.usecase.trading.UpdateTradeUseCase
import com.alphadoer.trader.presentation.trading.TradingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TradingViewModel @Inject constructor(
    private val recordTradeUseCase: RecordTradeUseCase,
    private val updateTradeUseCase: UpdateTradeUseCase,
    private val getTradesByDateUseCase: GetTradesByDateUseCase,
    private val getTradingStatisticsUseCase: GetTradingStatisticsUseCase,
    private val calculateProfitLossUseCase: CalculateProfitLossUseCase,
    private val processManager: ProcessManager,
    private val newsAnalysisRepository: NewsAnalysisRepository,
    private val stockRepository: StockRepository
) : ViewModel() {
    
    var currentStepId: String? = null
        private set
    
    private val _uiState = MutableStateFlow(TradingUiState())
    val uiState: StateFlow<TradingUiState> = _uiState.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    init {
        loadTodayTrades()
        loadRecommendedStocks()
    }
    
    fun loadTodayTrades() {
        val today = dateFormat.format(Date())
        viewModelScope.launch {
            getTradesByDateUseCase(today)
                .catch { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
                .collect { trades ->
                    _uiState.update { it.copy(trades = trades) }
                    loadStatistics(today)
                }
        }
    }
    
    private fun loadStatistics(date: String) {
        viewModelScope.launch {
            try {
                val statistics = getTradingStatisticsUseCase(date)
                _uiState.update { it.copy(statistics = statistics) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
    
    fun setStepId(stepId: String) {
        currentStepId = stepId
        // 开始步骤
        viewModelScope.launch {
            processManager.startStep(stepId)
        }
    }
    
    fun recordTrade(record: TradeRecord) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            recordTradeUseCase(record)
                .onSuccess {
                    val advice = com.alphadoer.trader.data.util.TradeAdviceGenerator.generateForTrade(record)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            showSuccessMessage = true,
                            adviceMessages = advice
                        )
                    }
                    loadTodayTrades()
                    
                    // 更新流程步骤状态（如果有）
                    currentStepId?.let { stepId ->
                        processManager.completeStep(
                            stepId = stepId,
                            notes = "已记录交易：${record.stockName} ${if (record.isBuy()) "买入" else "卖出"} ${record.quantity}股"
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
    
    fun updateTrade(record: TradeRecord) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            updateTradeUseCase(record)
                .onSuccess {
                    val advice = com.alphadoer.trader.data.util.TradeAdviceGenerator.generateForTrade(record)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            showSuccessMessage = true,
                            adviceMessages = advice
                        )
                    }
                    loadTodayTrades()
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
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun clearSuccessMessage() {
        _uiState.update { it.copy(showSuccessMessage = false) }
    }
    
    private fun loadRecommendedStocks() {
        viewModelScope.launch {
            try {
                // 获取今日的分析结果
                val today = dateFormat.format(Date())
                val allAnalyses = newsAnalysisRepository.getAnalysisHistory()
                    .first()
                
                // 查找今日的分析结果
                val todayAnalysis = allAnalyses.firstOrNull { analysis ->
                    val analysisDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Date(analysis.createdAt))
                    analysisDate == today
                }
                
                // 获取推荐股票
                val recommendedStocks = todayAnalysis?.recommendedStocks ?: emptyList()
                
                // 同时加载收藏的股票作为备选
                val favoriteStocks = stockRepository.getFavoriteStocks()
                    .first()
                
                // 合并推荐股票和收藏股票（去重）
                val allStocks = (recommendedStocks + favoriteStocks)
                    .distinctBy { it.stockCode }
                
                _uiState.update { it.copy(recommendedStocks = allStocks) }
            } catch (e: Exception) {
                // 如果加载失败，只加载收藏股票
                viewModelScope.launch {
                    try {
                        val favoriteStocks = stockRepository.getFavoriteStocks().first()
                        _uiState.update { it.copy(recommendedStocks = favoriteStocks) }
                    } catch (ex: Exception) {
                        // 忽略错误，使用空列表
                        _uiState.update { it.copy(recommendedStocks = emptyList()) }
                    }
                }
            }
        }
    }
}
