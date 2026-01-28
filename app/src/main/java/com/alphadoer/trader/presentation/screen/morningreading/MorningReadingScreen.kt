package com.alphadoer.trader.presentation.screen.morningreading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.alphadoer.trader.presentation.morningreading.NewsTemplate
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphadoer.trader.presentation.viewmodel.morningreading.MorningReadingViewModel

/**
 * 早间信息阅读主屏幕
 */
@Composable
fun MorningReadingScreen(
    viewModel: MorningReadingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 捕获ViewModel初始化异常
    LaunchedEffect(Unit) {
        try {
            // 确保ViewModel已初始化
        } catch (e: Exception) {
            Log.e("MorningReadingScreen", "ViewModel初始化异常: ${e.message}", e)
            e.printStackTrace()
        }
    }
    
    // 显示错误消息
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            try {
                snackbarHostState.showSnackbar(message)
                viewModel.handleEvent(com.alphadoer.trader.presentation.morningreading.MorningReadingEvent.ClearError)
            } catch (e: Exception) {
                Log.e("MorningReadingScreen", "显示错误消息失败: ${e.message}", e)
            }
        }
    }
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                Snackbar(snackbarData = snackbarData)
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 上部：新闻输入面板
            NewsInputPanel(
                newsText = uiState.newsText,
                onNewsTextChanged = { text ->
                    try {
                        viewModel.handleEvent(
                            com.alphadoer.trader.presentation.morningreading.MorningReadingEvent.NewsTextChanged(text)
                        )
                    } catch (e: Exception) {
                        Log.e("MorningReadingScreen", "处理文本变化失败: ${e.message}", e)
                    }
                },
                onUseTemplate = { template ->
                    try {
                        viewModel.handleEvent(
                            com.alphadoer.trader.presentation.morningreading.MorningReadingEvent.UseTemplate(
                                templateType = template
                            )
                        )
                    } catch (e: Exception) {
                        Log.e("MorningReadingScreen", "使用模板失败: ${e.message}", e)
                    }
                },
                onNewsSourceChanged = { src ->
                    viewModel.handleEvent(
                        com.alphadoer.trader.presentation.morningreading.MorningReadingEvent.NewsSourceChanged(src)
                    )
                },
                onNewsUrlChanged = { url ->
                    viewModel.handleEvent(
                        com.alphadoer.trader.presentation.morningreading.MorningReadingEvent.NewsUrlChanged(url)
                    )
                },
                onNewsTimeChanged = { time ->
                    viewModel.handleEvent(
                        com.alphadoer.trader.presentation.morningreading.MorningReadingEvent.NewsTimeChanged(time)
                    )
                },
                onNewsTagsChanged = { tagsCsv ->
                    viewModel.handleEvent(
                        com.alphadoer.trader.presentation.morningreading.MorningReadingEvent.NewsTagsChanged(tagsCsv)
                    )
                },
                newsSource = uiState.newsSource,
                newsUrl = uiState.newsUrl,
                newsTime = uiState.newsTime,
                newsTagsCsv = uiState.newsTagsCsv,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            
            // 中部：分析控制面板
            AnalysisControlPanel(
                canAnalyze = uiState.canAnalyze,
                isLoading = uiState.loadingState == com.alphadoer.trader.presentation.morningreading.LoadingState.LOADING,
                analysisHistory = uiState.analysisHistory,
                onAnalyzeClick = {
                    try {
                        viewModel.handleEvent(
                            com.alphadoer.trader.presentation.morningreading.MorningReadingEvent.AnalyzeNews
                        )
                    } catch (e: Exception) {
                        Log.e("MorningReadingScreen", "开始分析失败: ${e.message}", e)
                    }
                },
                onHistoryItemClick = { analysisId ->
                    try {
                        viewModel.handleEvent(
                            com.alphadoer.trader.presentation.morningreading.MorningReadingEvent.ViewHistoryDetail(analysisId)
                        )
                    } catch (e: Exception) {
                        Log.e("MorningReadingScreen", "查看历史详情失败: ${e.message}", e)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            
            // 下部：结果展示面板
            AnalysisResultPanel(
                analysis = uiState.currentAnalysis,
                loadingState = uiState.loadingState,
                onApplyToPlan = { analysisId ->
                    try {
                        viewModel.handleEvent(
                            com.alphadoer.trader.presentation.morningreading.MorningReadingEvent.ApplyToPlan(analysisId)
                        )
                    } catch (e: Exception) {
                        Log.e("MorningReadingScreen", "应用到计划失败: ${e.message}", e)
                    }
                },
                onLinkToSectorRecords = { analysisId ->
                    try {
                        viewModel.handleEvent(
                            com.alphadoer.trader.presentation.morningreading.MorningReadingEvent.LinkSectorsFromAnalysis(analysisId)
                        )
                    } catch (e: Exception) {
                        Log.e("MorningReadingScreen", "写入强势板块失败: ${e.message}", e)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
    
    // 历史分析详情弹窗
    uiState.currentAnalysis?.let { analysis ->
        if (uiState.showHistoryDialog) {
            AnalysisHistoryDialog(
                analysis = analysis,
                onDismiss = {
                    try {
                        viewModel.handleEvent(
                            com.alphadoer.trader.presentation.morningreading.MorningReadingEvent.DismissHistoryDialog
                        )
                    } catch (e: Exception) {
                        Log.e("MorningReadingScreen", "关闭对话框失败: ${e.message}", e)
                    }
                },
                onApplyToPlan = { analysisId ->
                    try {
                        viewModel.handleEvent(
                            com.alphadoer.trader.presentation.morningreading.MorningReadingEvent.ApplyToPlan(analysisId)
                        )
                    } catch (e: Exception) {
                        Log.e("MorningReadingScreen", "应用到计划失败: ${e.message}", e)
                    }
                }
            )
        }
    }
}
