package com.alphadoer.trader.presentation.screen.premarketplan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphadoer.trader.presentation.premarketplan.PreMarketPlanEvent
import com.alphadoer.trader.presentation.viewmodel.premarketplan.PreMarketPlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreMarketPlanScreen(
    viewModel: PreMarketPlanViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.handleEvent(PreMarketPlanEvent.ClearError)
        }
    }
    
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("盘前计划已保存")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("盘前计划") }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            if (uiState.isLoading) {
                FloatingActionButton(
                    onClick = { }
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                FloatingActionButton(
                    onClick = { viewModel.handleEvent(PreMarketPlanEvent.SavePlan) }
                ) {
                    Text("保存")
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 推荐股票列表
            if (uiState.recommendedStocks.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "早间分析推荐股票",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        for (stock in uiState.recommendedStocks.take(5)) {
                            Text(
                                text = "${stock.stockName} (${stock.stockCode}) - ${stock.reason}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // 重点关注股票
            Text(
                text = "重点关注股票",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            for (stock in uiState.focusStocks) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${stock.stockName} (${stock.stockCode})",
                                style = MaterialTheme.typography.titleSmall
                            )
                            TextButton(
                                onClick = {
                                    viewModel.handleEvent(PreMarketPlanEvent.RemoveFocusStock(stock.stockCode))
                                }
                            ) {
                                Text("删除")
                            }
                        }
                        Text(
                            text = "原因: ${stock.reason}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        if (stock.targetPrice != null) {
                            Text(
                                text = "目标价: ${stock.targetPrice}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            Button(
                onClick = {
                    // TODO: 打开添加股票对话框
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text("添加关注股票")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 交易策略
            OutlinedTextField(
                value = uiState.tradingStrategy,
                onValueChange = {
                    viewModel.handleEvent(PreMarketPlanEvent.TradingStrategyChanged(it))
                },
                label = { Text("交易策略") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                minLines = 3,
                maxLines = 5
            )
            
            // 风险控制
            OutlinedTextField(
                value = uiState.riskControl,
                onValueChange = {
                    viewModel.handleEvent(PreMarketPlanEvent.RiskControlChanged(it))
                },
                label = { Text("风险控制") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                minLines = 2,
                maxLines = 4
            )
            
            // 市场展望
            OutlinedTextField(
                value = uiState.marketOutlook,
                onValueChange = {
                    viewModel.handleEvent(PreMarketPlanEvent.MarketOutlookChanged(it))
                },
                label = { Text("市场展望") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                minLines = 2,
                maxLines = 4
            )
            
            // 备注
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = {
                    viewModel.handleEvent(PreMarketPlanEvent.NotesChanged(it))
                },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                minLines = 2,
                maxLines = 4
            )
        }
    }
}
