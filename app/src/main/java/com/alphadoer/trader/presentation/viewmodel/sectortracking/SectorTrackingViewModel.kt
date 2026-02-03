package com.alphadoer.trader.presentation.viewmodel.sectortracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphadoer.trader.domain.model.SectorSelectionRecord
import com.alphadoer.trader.domain.repository.SectorSelectionRepository
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
import dagger.hilt.android.lifecycle.HiltViewModel

data class SectorTrackingUiState(
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val selections: List<SectorSelectionRecord> = emptyList(),
    val inputSectorName: String = "",
    val inputSectorCode: String = "",
    val inputStockCodesCsv: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class SectorTrackingViewModel @Inject constructor(
    private val repository: SectorSelectionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SectorTrackingUiState())
    val uiState: StateFlow<SectorTrackingUiState> = _uiState.asStateFlow()

    init {
        observeToday()
    }

    private fun observeToday() {
        viewModelScope.launch {
            repository.getSelectionsByDate(_uiState.value.date).collect { list ->
                _uiState.update { it.copy(selections = list) }
            }
        }
    }

    fun updateSectorName(name: String) { _uiState.update { it.copy(inputSectorName = name) } }
    fun updateSectorCode(code: String) { _uiState.update { it.copy(inputSectorCode = code) } }
    fun updateStockCodesCsv(csv: String) { _uiState.update { it.copy(inputStockCodesCsv = csv) } }

    fun saveSelection() {
        val s = _uiState.value
        val stocks = s.inputStockCodesCsv.split(',', '，', '、').map { it.trim() }.filter { it.isNotEmpty() }
        if (s.inputSectorName.isBlank() || s.inputSectorCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请填写板块名称与代码") }
            return
        }
        if (stocks.size < 5) {
            _uiState.update { it.copy(errorMessage = "每个板块至少记录5只股票") }
            return
        }
        val record = SectorSelectionRecord(
            id = "sector_selection_${'$'}{s.date}_${'$'}{s.inputSectorCode}_${'$'}{UUID.randomUUID()}",
            date = s.date,
            sectorCode = s.inputSectorCode,
            sectorName = s.inputSectorName,
            stockCodes = stocks,
            notes = null
        )
        viewModelScope.launch {
            repository.saveSelection(record)
                .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
                .onSuccess {
                    _uiState.update { it.copy(inputSectorName = "", inputSectorCode = "", inputStockCodesCsv = "", errorMessage = null) }
                }
        }
    }

    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }

    fun deleteSelection(id: String) {
        viewModelScope.launch {
            repository.deleteSelection(id)
                .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
        }
    }
}
