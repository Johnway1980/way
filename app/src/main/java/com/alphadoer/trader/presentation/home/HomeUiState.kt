package com.alphadoer.trader.presentation.home

import com.alphadoer.trader.domain.model.process.DailyProcess
import com.alphadoer.trader.domain.model.process.ProcessStep

/**
 * 主界面UI状态
 */
data class HomeUiState(
    val process: DailyProcess? = null,
    val nextStep: ProcessStep? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
