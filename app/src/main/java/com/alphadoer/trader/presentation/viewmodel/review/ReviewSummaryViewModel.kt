package com.alphadoer.trader.presentation.viewmodel.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphadoer.trader.domain.model.review.DailyReviewSummary
import com.alphadoer.trader.domain.model.review.ImprovementPlan
import com.alphadoer.trader.domain.usecase.review.CreateTomorrowPlanUseCase
import com.alphadoer.trader.domain.usecase.review.GenerateDailySummaryUseCase
import com.alphadoer.trader.presentation.review.ReviewSummaryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReviewSummaryViewModel @Inject constructor(
    private val generateDailySummaryUseCase: GenerateDailySummaryUseCase,
    private val createTomorrowPlanUseCase: CreateTomorrowPlanUseCase,
    private val summarizeNewsAndSectorsUseCase: com.alphadoer.trader.domain.usecase.SummarizeNewsAndSectorsUseCase,
    private val tradeJournalRepository: com.alphadoer.trader.domain.repository.TradeJournalRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReviewSummaryUiState())
    val uiState: StateFlow<ReviewSummaryUiState> = _uiState.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    fun generateSummary(date: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            generateDailySummaryUseCase(date)
                .onSuccess { summary ->
                    _uiState.update { it.copy(summary = summary, isLoading = false) }
                    // 追加新闻与板块总结
                    summarizeNewsAndSectorsUseCase(date)
                        .onSuccess { insights ->
                            _uiState.update { state ->
                                state.copy(summary = state.summary?.copy(marketInsights = insights))
                            }
                            // 标记今日复盘完成并持久化
                            try {
                                val journal = tradeJournalRepository.getJournalByDate(date)
                                if (journal != null) {
                                    tradeJournalRepository.updateJournal(journal.copy(reviewCompleted = true))
                                } else {
                                    tradeJournalRepository.insertJournal(
                                        com.alphadoer.trader.domain.model.TradeJournal(
                                            date = date,
                                            morningConclusion = null,
                                            auctionFeeling = null,
                                            reviewCompleted = true
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                // 不中断流程，仅记录错误
                            }
                        }
                        .onFailure { err ->
                            _uiState.update { it.copy(errorMessage = err.message) }
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
    
    fun createTomorrowPlan(
        focusSectors: List<String>,
        focusStocks: List<String>,
        strategies: List<String>
    ) {
        viewModelScope.launch {
            val today = dateFormat.format(Date())
            createTomorrowPlanUseCase(today, focusSectors, focusStocks, strategies)
                .onSuccess { plan ->
                    _uiState.update { it.copy(tomorrowPlan = plan) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
