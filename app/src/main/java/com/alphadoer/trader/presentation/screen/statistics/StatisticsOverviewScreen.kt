package com.alphadoer.trader.presentation.screen.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphadoer.trader.presentation.viewmodel.statistics.StatisticsOverviewViewModel
import kotlinx.coroutines.launch

/**
 * 统计概览界面
 */
@Composable
fun StatisticsOverviewScreen(
    viewModel: StatisticsOverviewViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.loadPerformanceMetrics(StatisticsOverviewViewModel.TimeRange.ALL)
    }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearError()
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
                .padding(16.dp)
        ) {
            Text(
                text = "交易统计",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (uiState.isLoading) {
                Text(text = "加载中...")
            } else if (uiState.performance != null) {
                val performance = uiState.performance!!
                
                // 总收益卡片
                PerformanceCard(
                    title = "总收益率",
                    value = String.format("%.2f%%", performance.totalReturn)
                )
                
                // 总盈亏卡片
                PerformanceCard(
                    title = "总盈亏",
                    value = String.format("¥%.2f", performance.totalProfitLoss)
                )
                
                // 胜率卡片
                PerformanceCard(
                    title = "胜率",
                    value = String.format("%.1f%%", performance.winRate)
                )
                
                // 交易次数卡片
                PerformanceCard(
                    title = "交易次数",
                    value = performance.totalTrades.toString()
                )
                
                // 最大回撤卡片
                PerformanceCard(
                    title = "最大回撤",
                    value = String.format("%.2f%%", performance.maxDrawdown)
                )
            }
        }
    }
}

@Composable
private fun PerformanceCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
