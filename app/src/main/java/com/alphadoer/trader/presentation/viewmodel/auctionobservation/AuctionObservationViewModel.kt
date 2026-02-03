package com.alphadoer.trader.presentation.viewmodel.auctionobservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphadoer.trader.domain.model.AuctionObservation
import com.alphadoer.trader.domain.repository.TradeJournalRepository
import com.alphadoer.trader.domain.usecase.SaveAuctionObservationUseCase
import com.alphadoer.trader.domain.usecase.process.ProcessManager
import com.alphadoer.trader.presentation.auctionobservation.AuctionObservationEvent
import com.alphadoer.trader.presentation.auctionobservation.AuctionObservationUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AuctionObservationViewModel @Inject constructor(
    private val saveAuctionObservationUseCase: SaveAuctionObservationUseCase,
    private val tradeJournalRepository: TradeJournalRepository,
    private val processManager: ProcessManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuctionObservationUiState())
    val uiState: StateFlow<AuctionObservationUiState> = _uiState.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    init {
        loadObservation()
    }
    
    fun handleEvent(event: AuctionObservationEvent) {
        when (event) {
            is AuctionObservationEvent.MarketSentimentChanged -> {
                _uiState.update { it.copy(marketSentiment = event.sentiment) }
            }
            
            is AuctionObservationEvent.FeelingChanged -> {
                if (event.feeling in 1..5) {
                    _uiState.update { it.copy(feeling = event.feeling) }
                }
            }
            
            is AuctionObservationEvent.AddKeyObservation -> {
                if (event.observation.isNotBlank()) {
                    _uiState.update { state ->
                        state.copy(
                            keyObservations = state.keyObservations + event.observation
                        )
                    }
                }
            }
            
            is AuctionObservationEvent.RemoveKeyObservation -> {
                _uiState.update { state ->
                    state.copy(
                        keyObservations = state.keyObservations.filterIndexed { index, _ ->
                            index != event.index
                        }
                    )
                }
            }
            
            is AuctionObservationEvent.VolumeAnalysisChanged -> {
                _uiState.update { it.copy(volumeAnalysis = event.analysis) }
            }
            
            is AuctionObservationEvent.PriceTrendChanged -> {
                _uiState.update { it.copy(priceTrend = event.trend) }
            }
            
            is AuctionObservationEvent.AddStockObservation -> {
                _uiState.update { state ->
                    state.copy(
                        focusStocks = state.focusStocks + event.stock
                    )
                }
            }
            
            is AuctionObservationEvent.RemoveStockObservation -> {
                _uiState.update { state ->
                    state.copy(
                        focusStocks = state.focusStocks.filter { it.stockCode != event.stockCode }
                    )
                }
            }
            
            is AuctionObservationEvent.NotesChanged -> {
                _uiState.update { it.copy(notes = event.notes) }
            }
            
            is AuctionObservationEvent.SaveObservation -> {
                saveObservation()
            }
            
            is AuctionObservationEvent.LoadObservation -> {
                loadObservation()
            }
            
            is AuctionObservationEvent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }
    
    private fun loadObservation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val today = dateFormat.format(Date())
                val journal = tradeJournalRepository.getJournalByDate(today)
                journal?.auctionFeeling?.let { feeling ->
                    _uiState.update {
                        it.copy(
                            feeling = feeling,
                            isLoading = false
                        )
                    }
                } ?: run {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "加载观察记录失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun saveObservation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val today = dateFormat.format(Date())
                val observation = AuctionObservation(
                    id = _uiState.value.observation?.id ?: UUID.randomUUID().toString(),
                    date = today,
                    marketSentiment = _uiState.value.marketSentiment,
                    feeling = _uiState.value.feeling,
                    keyObservations = _uiState.value.keyObservations,
                    volumeAnalysis = _uiState.value.volumeAnalysis.takeIf { it.isNotBlank() },
                    priceTrend = _uiState.value.priceTrend,
                    focusStocks = _uiState.value.focusStocks,
                    notes = _uiState.value.notes.takeIf { it.isNotBlank() }
                )
                
                saveAuctionObservationUseCase(observation)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                observation = observation,
                                isLoading = false,
                                isSaved = true,
                                errorMessage = null
                            )
                        }
                        // 完成步骤
                        processManager.completeStep("step_3", notes = "集合竞价观察已保存")
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
}
