package com.alphadoer.trader.presentation.viewmodel.morningreading

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphadoer.trader.domain.model.AnalysisOptions
import com.alphadoer.trader.domain.model.NewsAnalysis
import com.alphadoer.trader.domain.repository.NewsAnalysisRepository
import com.alphadoer.trader.domain.repository.SectorSelectionRepository
import com.alphadoer.trader.domain.repository.StockRepository
import com.alphadoer.trader.domain.repository.TradeJournalRepository
import com.alphadoer.trader.domain.usecase.AnalyzeNewsUseCase
import com.alphadoer.trader.domain.usecase.GetAnalysisHistoryUseCase
import com.alphadoer.trader.domain.usecase.SaveAnalysisResultUseCase
import com.alphadoer.trader.presentation.morningreading.LoadingState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.alphadoer.trader.presentation.morningreading.MorningReadingEvent
import com.alphadoer.trader.presentation.morningreading.MorningReadingUiState
import com.alphadoer.trader.presentation.morningreading.NewsTemplate
import com.alphadoer.trader.data.util.SectorStockPool
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MorningReadingViewModel @Inject constructor(
    private val analyzeNewsUseCase: AnalyzeNewsUseCase,
    private val getAnalysisHistoryUseCase: GetAnalysisHistoryUseCase,
    private val saveAnalysisResultUseCase: SaveAnalysisResultUseCase,
    private val newsAnalysisRepository: NewsAnalysisRepository,
    private val stockRepository: StockRepository,
    private val tradeJournalRepository: TradeJournalRepository,
    private val sectorSelectionRepository: SectorSelectionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MorningReadingUiState())
    val uiState: StateFlow<MorningReadingUiState> = _uiState.asStateFlow()
    
    init {
        Log.d("MorningReadingViewModel", "========== ViewModel初始化开始 ==========")
        try {
            Log.d("MorningReadingViewModel", "开始加载分析历史...")
            loadAnalysisHistory()
            Log.d("MorningReadingViewModel", "分析历史加载已启动")
        } catch (e: Exception) {
            Log.e("MorningReadingViewModel", "初始化失败: ${e.message}", e)
            e.printStackTrace()
            // 初始化失败不影响UI显示，只是历史记录为空
        }
        Log.d("MorningReadingViewModel", "========== ViewModel初始化完成 ==========")
    }
    
    fun handleEvent(event: MorningReadingEvent) {
        try {
            when (event) {
            is MorningReadingEvent.NewsTextChanged -> {
                _uiState.update { it.copy(newsText = event.text) }
            }
            is MorningReadingEvent.NewsSourceChanged -> {
                _uiState.update { it.copy(newsSource = event.source) }
            }
            is MorningReadingEvent.NewsUrlChanged -> {
                _uiState.update { it.copy(newsUrl = event.url) }
            }
            is MorningReadingEvent.NewsTimeChanged -> {
                _uiState.update { it.copy(newsTime = event.time) }
            }
            is MorningReadingEvent.NewsTagsChanged -> {
                _uiState.update { it.copy(newsTagsCsv = event.tagsCsv) }
            }
            
            is MorningReadingEvent.AnalysisOptionsChanged -> {
                _uiState.update { it.copy(analysisOptions = event.options) }
            }
            
            is MorningReadingEvent.AnalyzeNews -> {
                analyzeNews()
            }
            
            is MorningReadingEvent.SaveAnalysis -> {
                saveAnalysis(event.analysis)
            }
            
            is MorningReadingEvent.ApplyToPlan -> {
                applyToPlan(event.analysisId)
            }

            is MorningReadingEvent.LinkSectorsFromAnalysis -> {
                linkSectorsFromAnalysis(event.analysisId)
            }
            
            is MorningReadingEvent.ViewHistoryDetail -> {
                viewHistoryDetail(event.analysisId)
            }
            
            is MorningReadingEvent.DismissHistoryDialog -> {
                _uiState.update { it.copy(showHistoryDialog = false, selectedHistoryId = null) }
            }
            
            is MorningReadingEvent.DeleteAnalysis -> {
                deleteAnalysis(event.analysisId)
            }
            
            is MorningReadingEvent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            
            is MorningReadingEvent.UseTemplate -> {
                useTemplate(event.templateType)
            }
            is MorningReadingEvent.SetHistoryDateFilter -> {
                val date = event.date
                _uiState.update { it.copy(historyDateFilter = date) }
                applyHistoryFilter(date)
            }
            is MorningReadingEvent.ViewHistoryByDate -> {
                val date = event.date
                _uiState.update { it.copy(historyDateFilter = date) }
                applyHistoryFilter(date)
                _uiState.update { it.copy(showHistoryDialog = true) }
            }
            }
        } catch (e: Exception) {
            Log.e("MorningReadingViewModel", "处理事件失败: ${e.message}", e)
            e.printStackTrace()
            _uiState.update {
                it.copy(errorMessage = "操作失败: ${e.message ?: "未知错误"}")
            }
        }
    }

    private fun linkSectorsFromAnalysis(analysisId: String) {
        viewModelScope.launch {
            try {
                val analysis = _uiState.value.currentAnalysis?.takeIf { it.id == analysisId }
                    ?: _uiState.value.analysisHistory.find { it.id == analysisId }
                    ?: newsAnalysisRepository.getAnalysisById(analysisId)

                if (analysis == null) {
                    _uiState.update { it.copy(errorMessage = "未找到分析结果") }
                    return@launch
                }

                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                // 按板块保存所有推荐板块的股票
                val sortedSectors = analysis.affectedSectors.sortedByDescending {
                    when (it.impactLevel) {
                        com.alphadoer.trader.domain.model.AffectedSector.ImpactLevel.HIGH -> 3
                        com.alphadoer.trader.domain.model.AffectedSector.ImpactLevel.MEDIUM -> 2
                        else -> 1
                    }
                }

                var success = 0
                var fail = 0

                for (sector in sortedSectors) {
                    val sectorName = sector.sectorName
                    val initialCodes = analysis.recommendedStocks
                        .filter { it.sectorName == sectorName }
                        .mapNotNull { it.stockCode }
                        .filter { it.isNotBlank() }
                        .distinct()

                    val finalCodes = initialCodes.toMutableList()

                    // 从股票池补齐至≥5只
                    if (finalCodes.size < 5) {
                        val candidates = SectorStockPool.getStocksForSector(sectorName)
                            .map { it.stockCode }
                            .filter { it.isNotBlank() && it !in finalCodes }
                        finalCodes.addAll(candidates.take(5 - finalCodes.size))
                    }

                    // 如果仍不足，保留现有（向后兼容）
                    if (finalCodes.isEmpty()) {
                        continue
                    }

                    val record = com.alphadoer.trader.domain.model.SectorSelectionRecord(
                        id = "sector_selection_${'$'}date_${'$'}{sector.sectorCode}_${'$'}{UUID.randomUUID()}",
                        date = date,
                        sectorCode = sector.sectorCode,
                        sectorName = sectorName,
                        stockCodes = finalCodes,
                        notes = "来自早读分析自动写入"
                    )

                    sectorSelectionRepository.saveSelection(record)
                        .onSuccess { success++ }
                        .onFailure { fail++ }
                }

                val msg = if (fail == 0) {
                    "已写入 ${success} 个板块到强势板块记录"
                } else {
                    "已写入 ${success} 个板块，${fail} 个失败"
                }
                _uiState.update { it.copy(errorMessage = msg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "写入强势板块失败: ${'$'}{e.message}") }
            }
        }
    }
    
    private fun analyzeNews() {
        val newsText = _uiState.value.newsText
        if (newsText.isBlank()) {
            Log.w("MorningReadingViewModel", "新闻内容为空")
            _uiState.update { it.copy(errorMessage = "请输入新闻内容") }
            return
        }
        
        Log.d("MorningReadingViewModel", "开始分析新闻，内容长度: ${newsText.length}")
        
        _uiState.update { 
            it.copy(
                loadingState = LoadingState.LOADING,
                errorMessage = null
            )
        }
        
        viewModelScope.launch {
            try {
                Log.d("MorningReadingViewModel", "调用analyzeNewsUseCase")
                analyzeNewsUseCase(newsText, _uiState.value.analysisOptions)
                    .onSuccess { analysis ->
                        Log.d("MorningReadingViewModel", "分析成功，ID: ${analysis.id}")
                        Log.d("MorningReadingViewModel", "摘要: ${analysis.summary.take(100)}")
                        // 合并用户提供的新闻元数据
                        val meta = (analysis.metadata ?: emptyMap()).toMutableMap()
                        _uiState.value.newsSource.takeIf { it.isNotBlank() }?.let { meta["user_source"] = it }
                        _uiState.value.newsUrl.takeIf { it.isNotBlank() }?.let { meta["user_url"] = it }
                        _uiState.value.newsTime.takeIf { it.isNotBlank() }?.let { meta["user_time"] = it }
                        _uiState.value.newsTagsCsv.takeIf { it.isNotBlank() }?.let { meta["user_tags"] = it }
                        val enriched = analysis.copy(metadata = meta)
                        _uiState.update {
                            it.copy(
                                currentAnalysis = enriched,
                                loadingState = LoadingState.SUCCESS,
                                errorMessage = null
                            )
                        }
                        // 自动保存分析结果
                        saveAnalysis(enriched)
                    }
                    .onFailure { error ->
                        Log.e("MorningReadingViewModel", "分析失败: ${error.message}", error)
                        _uiState.update {
                            it.copy(
                                loadingState = LoadingState.ERROR,
                                errorMessage = "分析失败: ${error.message ?: "未知错误"}，已使用模拟数据"
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e("MorningReadingViewModel", "分析过程发生异常: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        loadingState = LoadingState.ERROR,
                        errorMessage = "分析过程发生异常: ${e.message ?: "未知错误"}"
                    )
                }
            }
        }
    }
    
    private fun saveAnalysis(analysis: NewsAnalysis) {
        viewModelScope.launch {
            saveAnalysisResultUseCase(analysis)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = "保存失败: ${error.message}")
                    }
                }
        }
    }
    
    private fun applyToPlan(analysisId: String) {
        viewModelScope.launch {
            try {
                // 获取分析结果
                val analysis = _uiState.value.currentAnalysis?.takeIf { it.id == analysisId }
                    ?: _uiState.value.analysisHistory.find { it.id == analysisId }
                    ?: newsAnalysisRepository.getAnalysisById(analysisId)
                
                if (analysis == null) {
                    _uiState.update { it.copy(errorMessage = "未找到分析结果") }
                    return@launch
                }
                
                // 将推荐股票添加到收藏/观察列表
                var successCount = 0
                var failCount = 0
                
                analysis.recommendedStocks.forEach { recommendedStock ->
                    try {
                        // 先尝试获取股票信息，如果不存在则创建
                        val existingStock = try {
                            stockRepository.getStockByCode(recommendedStock.stockCode)
                        } catch (e: Exception) {
                            Log.e("MorningReadingViewModel", "获取股票信息失败: ${e.message}", e)
                            null
                        }
                        
                        if (existingStock == null) {
                            // 如果股票不存在，使用完整的推荐股票信息创建
                            stockRepository.saveStock(recommendedStock)
                                .onSuccess { successCount++ }
                                .onFailure { 
                                    failCount++
                                    Log.e("MorningReadingViewModel", "保存股票失败: ${it.message}", it)
                                }
                        } else {
                            // 如果已存在，确保在收藏列表中
                            stockRepository.addToFavorites(recommendedStock.stockCode)
                                .onSuccess { successCount++ }
                                .onFailure { 
                                    failCount++
                                    Log.e("MorningReadingViewModel", "添加到收藏失败: ${it.message}", it)
                                }
                        }
                    } catch (e: Exception) {
                        Log.e("MorningReadingViewModel", "处理推荐股票失败: ${e.message}", e)
                        failCount++
                    }
                }
                
                // 保存分析摘要到今日交易日志
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val today = dateFormat.format(Date())
                    
                    val journal = try {
                        tradeJournalRepository.getJournalByDate(today)
                    } catch (e: Exception) {
                        Log.e("MorningReadingViewModel", "获取交易日志失败: ${e.message}", e)
                        null
                    }
                    
                    if (journal != null) {
                        // 更新现有日志
                        try {
                            val updatedJournal = journal.copy(
                                morningConclusion = analysis.summary
                            )
                            tradeJournalRepository.updateJournal(updatedJournal)
                        } catch (e: Exception) {
                            Log.e("MorningReadingViewModel", "更新交易日志失败: ${e.message}", e)
                        }
                    } else {
                        // 创建新日志
                        try {
                            val newJournal = com.alphadoer.trader.domain.model.TradeJournal(
                                date = today,
                                morningConclusion = analysis.summary,
                                auctionFeeling = null,
                                reviewCompleted = false
                            )
                            tradeJournalRepository.insertJournal(newJournal)
                        } catch (e: Exception) {
                            Log.e("MorningReadingViewModel", "创建交易日志失败: ${e.message}", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MorningReadingViewModel", "保存交易日志失败: ${e.message}", e)
                    // 不影响整体流程，继续执行
                }
                
                // 更新UI状态
                val message = if (failCount == 0) {
                    "已成功应用 ${successCount} 只推荐股票到交易计划"
                } else {
                    "已应用 ${successCount} 只股票，${failCount} 只失败"
                }
                _uiState.update { 
                    it.copy(
                        errorMessage = null,
                        currentAnalysis = analysis // 确保当前分析已更新
                    ) 
                }
                // 显示成功消息（通过errorMessage字段，实际应该用successMessage）
                _uiState.update { it.copy(errorMessage = message) }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "应用到计划失败: ${e.message}") 
                }
            }
        }
    }
    
    private fun viewHistoryDetail(analysisId: String) {
        try {
            val analysis = _uiState.value.analysisHistory.find { it.id == analysisId }
            if (analysis != null) {
                _uiState.update {
                    it.copy(
                        selectedHistoryId = analysisId,
                        showHistoryDialog = true,
                        currentAnalysis = analysis
                    )
                }
            } else {
                Log.w("MorningReadingViewModel", "未找到分析历史: $analysisId")
            }
        } catch (e: Exception) {
            Log.e("MorningReadingViewModel", "查看历史详情失败: ${e.message}", e)
            e.printStackTrace()
            _uiState.update {
                it.copy(errorMessage = "查看历史详情失败: ${e.message ?: "未知错误"}")
            }
        }
    }
    
    private fun deleteAnalysis(analysisId: String) {
        viewModelScope.launch {
            try {
                // TODO: 实现删除逻辑
                loadAnalysisHistory()
            } catch (e: Exception) {
                Log.e("MorningReadingViewModel", "删除分析失败: ${e.message}", e)
                e.printStackTrace()
                _uiState.update {
                    it.copy(errorMessage = "删除分析失败: ${e.message ?: "未知错误"}")
                }
            }
        }
    }
    
    private fun loadAnalysisHistory() {
        Log.d("MorningReadingViewModel", "loadAnalysisHistory() 被调用")
        viewModelScope.launch {
            try {
                Log.d("MorningReadingViewModel", "开始收集分析历史Flow...")
                getAnalysisHistoryUseCase()
                    .catch { error ->
                        Log.e("MorningReadingViewModel", "Flow收集错误: ${error.message}", error)
                        error.printStackTrace()
                        _uiState.update {
                            it.copy(errorMessage = "加载历史失败: ${error.message}")
                        }
                    }
                    .collect { history ->
                        Log.d("MorningReadingViewModel", "收到分析历史，数量: ${history.size}")
                        _uiState.update { it.copy(analysisHistory = history) }
                        applyHistoryFilter(_uiState.value.historyDateFilter)
                    }
            } catch (e: Exception) {
                Log.e("MorningReadingViewModel", "loadAnalysisHistory异常: ${e.message}", e)
                e.printStackTrace()
                _uiState.update {
                    it.copy(errorMessage = "加载历史失败: ${e.message}")
                }
            }
        }
    }

    private fun applyHistoryFilter(date: String?) {
        val all = _uiState.value.analysisHistory
        val filtered = if (date.isNullOrBlank()) {
            all
        } else {
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            all.filter { a ->
                try {
                    df.format(Date(a.createdAt)) == date
                } catch (_: Exception) { false }
            }
        }
        _uiState.update { it.copy(filteredHistory = filtered) }
    }
    
    private fun useTemplate(templateType: NewsTemplate) {
        try {
            val template = when (templateType) {
                NewsTemplate.INTERNATIONAL -> """
                    国际新闻模板：
                    [请在此输入国际新闻内容，包括：
                    - 事件概述
                    - 影响范围
                    - 市场反应预期]
                """.trimIndent()
                
                NewsTemplate.DOMESTIC_POLICY -> """
                    国内政策模板：
                    [请在此输入国内政策新闻，包括：
                    - 政策内容
                    - 发布机构
                    - 影响行业]
                """.trimIndent()
                
                NewsTemplate.INDUSTRY -> """
                    行业动态模板：
                    [请在此输入行业动态，包括：
                    - 行业事件
                    - 相关公司
                    - 市场影响]
                """.trimIndent()
                
                NewsTemplate.COMPANY -> """
                    公司公告模板：
                    [请在此输入公司公告，包括：
                    - 公告类型
                    - 主要内容
                    - 影响分析]
                """.trimIndent()
            }
            
            _uiState.update { it.copy(newsText = template) }
        } catch (e: Exception) {
            Log.e("MorningReadingViewModel", "使用模板失败: ${e.message}", e)
            e.printStackTrace()
            _uiState.update {
                it.copy(errorMessage = "使用模板失败: ${e.message ?: "未知错误"}")
            }
        }
    }
}
