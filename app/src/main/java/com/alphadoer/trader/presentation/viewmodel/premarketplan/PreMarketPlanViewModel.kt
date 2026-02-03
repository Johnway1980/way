package com.alphadoer.trader.presentation.viewmodel.premarketplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphadoer.trader.domain.model.PreMarketPlan
import com.alphadoer.trader.domain.model.RecommendedStock
import com.alphadoer.trader.domain.repository.NewsAnalysisRepository
import com.alphadoer.trader.domain.repository.StockRepository
import com.alphadoer.trader.domain.usecase.GetPreMarketPlanUseCase
import com.alphadoer.trader.domain.usecase.SavePreMarketPlanUseCase
import com.alphadoer.trader.domain.usecase.process.ProcessManager
import com.alphadoer.trader.presentation.premarketplan.PreMarketPlanEvent
import com.alphadoer.trader.presentation.premarketplan.PreMarketPlanUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PreMarketPlanViewModel @Inject constructor(
    private val savePreMarketPlanUseCase: SavePreMarketPlanUseCase,
    private val getPreMarketPlanUseCase: GetPreMarketPlanUseCase,
    private val newsAnalysisRepository: NewsAnalysisRepository,
    private val stockRepository: StockRepository,
    private val processManager: ProcessManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PreMarketPlanUiState())
    val uiState: StateFlow<PreMarketPlanUiState> = _uiState.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    init {
        loadPlan()
        loadRecommendedStocks()
    }
    
    fun handleEvent(event: PreMarketPlanEvent) {
        when (event) {
            is PreMarketPlanEvent.AddFocusStock -> {
                _uiState.update { state ->
                    state.copy(
                        focusStocks = state.focusStocks + event.stock
                    )
                }
            }
            
            is PreMarketPlanEvent.RemoveFocusStock -> {
                _uiState.update { state ->
                    state.copy(
                        focusStocks = state.focusStocks.filter { it.stockCode != event.stockCode }
                    )
                }
            }
            
            is PreMarketPlanEvent.UpdateFocusStock -> {
                _uiState.update { state ->
                    state.copy(
                        focusStocks = state.focusStocks.map { 
                            if (it.stockCode == event.stock.stockCode) event.stock else it
                        }
                    )
                }
            }
            
            is PreMarketPlanEvent.TradingStrategyChanged -> {
                _uiState.update { it.copy(tradingStrategy = event.strategy) }
            }
            
            is PreMarketPlanEvent.RiskControlChanged -> {
                _uiState.update { it.copy(riskControl = event.riskControl) }
            }
            
            is PreMarketPlanEvent.MarketOutlookChanged -> {
                _uiState.update { it.copy(marketOutlook = event.outlook) }
            }
            
            is PreMarketPlanEvent.NotesChanged -> {
                _uiState.update { it.copy(notes = event.notes) }
            }
            
            is PreMarketPlanEvent.SavePlan -> {
                savePlan()
            }
            
            is PreMarketPlanEvent.LoadPlan -> {
                loadPlan()
            }
            
            is PreMarketPlanEvent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }
    
    private fun loadPlan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val today = dateFormat.format(Date())
                val plan = getPreMarketPlanUseCase(today)
                if (plan != null) {
                    _uiState.update {
                        it.copy(
                            plan = plan,
                            focusStocks = plan.focusStocks,
                            tradingStrategy = plan.tradingStrategy ?: "",
                            riskControl = plan.riskControl ?: "",
                            marketOutlook = plan.marketOutlook ?: "",
                            notes = plan.notes ?: "",
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "加载计划失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun savePlan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val today = dateFormat.format(Date())
                val plan = PreMarketPlan(
                    id = _uiState.value.plan?.id ?: UUID.randomUUID().toString(),
                    date = today,
                    focusStocks = _uiState.value.focusStocks,
                    tradingStrategy = _uiState.value.tradingStrategy.takeIf { it.isNotBlank() },
                    riskControl = _uiState.value.riskControl.takeIf { it.isNotBlank() },
                    marketOutlook = _uiState.value.marketOutlook.takeIf { it.isNotBlank() },
                    notes = _uiState.value.notes.takeIf { it.isNotBlank() }
                )
                
                savePreMarketPlanUseCase(plan)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                plan = plan,
                                isLoading = false,
                                isSaved = true,
                                errorMessage = null
                            )
                        }
                        // 完成步骤
                        processManager.completeStep("step_2", notes = "盘前计划已保存")
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "保存失败: ${error.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "保存失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun loadRecommendedStocks() {
        viewModelScope.launch {
            try {
                val today = dateFormat.format(Date())
                val allAnalyses = newsAnalysisRepository.getAnalysisHistory().first()
                val todayAnalysis = allAnalyses.firstOrNull { analysis ->
                    val analysisDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Date(analysis.createdAt))
                    analysisDate == today
                }
                
                val recommendedStocks = todayAnalysis?.recommendedStocks ?: emptyList()
                _uiState.update { it.copy(recommendedStocks = recommendedStocks) }
            } catch (e: Exception) {
                // 忽略错误
            }
        }
    }
}
