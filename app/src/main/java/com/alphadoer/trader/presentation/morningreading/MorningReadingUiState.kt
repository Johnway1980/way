package com.alphadoer.trader.presentation.morningreading

import com.alphadoer.trader.domain.model.AnalysisOptions
import com.alphadoer.trader.domain.model.NewsAnalysis

/**
 * 早间阅读UI状态
 */
data class MorningReadingUiState(
    val newsText: String = "",
    val newsSource: String = "",
    val newsUrl: String = "",
    val newsTime: String = "", // yyyy-MM-dd HH:mm
    val newsTagsCsv: String = "", // 逗号分隔标签
    val analysisOptions: AnalysisOptions = AnalysisOptions(),
    val currentAnalysis: NewsAnalysis? = null,
    val analysisHistory: List<NewsAnalysis> = emptyList(),
    val filteredHistory: List<NewsAnalysis> = emptyList(),
    val loadingState: LoadingState = LoadingState.IDLE,
    val errorMessage: String? = null,
    val selectedHistoryId: String? = null,
    val showHistoryDialog: Boolean = false,
    val historyDateFilter: String? = null // yyyy-MM-dd
) {
    val canAnalyze: Boolean
        get() = newsText.isNotBlank() && loadingState != LoadingState.LOADING
    
    val hasAnalysisResult: Boolean
        get() = currentAnalysis != null
}

/**
 * 加载状态
 */
enum class LoadingState {
    IDLE,       // 空闲
    LOADING,    // 加载中
    SUCCESS,    // 成功
    ERROR       // 错误
}
