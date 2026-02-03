package com.alphadoer.trader.presentation.screen.trading

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphadoer.trader.presentation.trading.TradingUiState
import com.alphadoer.trader.presentation.viewmodel.trading.TradingViewModel
import kotlinx.coroutines.launch

/**
 * 交易主界面
 */
@Composable
fun TradingScreen(
    viewModel: TradingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(TradingUiState.TradingTab.QUICK_RECORD) }
    
    // 显示错误消息
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearError()
            }
        }
    }
    
    // 显示成功消息
    LaunchedEffect(uiState.showSuccessMessage) {
        if (uiState.showSuccessMessage) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("交易记录已保存")
                viewModel.clearSuccessMessage()
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
        ) {
            // 标签页
            ScrollableTabRow(selectedTabIndex = selectedTab.ordinal) {
                TradingUiState.TradingTab.values().forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { 
                            Text(
                                when (tab) {
                                    TradingUiState.TradingTab.QUICK_RECORD -> "快速记录"
                                    TradingUiState.TradingTab.TRADE_LIST -> "交易列表"
                                    TradingUiState.TradingTab.STATISTICS -> "实时统计"
                                }
                            )
                        }
                    )
                }
            }
            
            // 内容区域
            when (selectedTab) {
                TradingUiState.TradingTab.QUICK_RECORD -> {
                    QuickTradeForm(
                        onRecordTrade = { record ->
                            viewModel.recordTrade(record)
                        }
                    )
                }
                TradingUiState.TradingTab.TRADE_LIST -> {
                    TradeListComponent(
                        trades = uiState.trades,
                        onTradeClick = { /* TODO: 查看详情 */ }
                    )
                }
                TradingUiState.TradingTab.STATISTICS -> {
                    TradingStatsPanel(
                        statistics = uiState.statistics
                    )
                }
            }
        }
    }
}
