package com.alphadoer.trader.presentation.screen.auctionobservation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphadoer.trader.domain.model.AuctionObservation
import com.alphadoer.trader.presentation.auctionobservation.AuctionObservationEvent
import com.alphadoer.trader.presentation.viewmodel.auctionobservation.AuctionObservationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionObservationScreen(
    viewModel: AuctionObservationViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.handleEvent(AuctionObservationEvent.ClearError)
        }
    }
    
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("集合竞价观察已保存")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("集合竞价观察") }
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
                    onClick = { viewModel.handleEvent(AuctionObservationEvent.SaveObservation) }
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
            // 市场情绪
            Text(
                text = "市场情绪",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = uiState.marketSentiment == AuctionObservation.MarketSentiment.BULLISH,
                    onClick = {
                        viewModel.handleEvent(
                            AuctionObservationEvent.MarketSentimentChanged(
                                AuctionObservation.MarketSentiment.BULLISH
                            )
                        )
                    },
                    label = { Text("看涨") }
                )
                FilterChip(
                    selected = uiState.marketSentiment == AuctionObservation.MarketSentiment.NEUTRAL,
                    onClick = {
                        viewModel.handleEvent(
                            AuctionObservationEvent.MarketSentimentChanged(
                                AuctionObservation.MarketSentiment.NEUTRAL
                            )
                        )
                    },
                    label = { Text("中性") }
                )
                FilterChip(
                    selected = uiState.marketSentiment == AuctionObservation.MarketSentiment.BEARISH,
                    onClick = {
                        viewModel.handleEvent(
                            AuctionObservationEvent.MarketSentimentChanged(
                                AuctionObservation.MarketSentiment.BEARISH
                            )
                        )
                    },
                    label = { Text("看跌") }
                )
            }
            
            // 感受评分
            Text(
                text = "感受评分: ${uiState.feeling}/5",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (1..5).forEach { score ->
                    FilterChip(
                        selected = uiState.feeling == score,
                        onClick = {
                            viewModel.handleEvent(AuctionObservationEvent.FeelingChanged(score))
                        },
                        label = { Text("$score") }
                    )
                }
            }
            
            // 价格趋势
            Text(
                text = "价格趋势",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = uiState.priceTrend == AuctionObservation.PriceTrend.RISING,
                    onClick = {
                        viewModel.handleEvent(
                            AuctionObservationEvent.PriceTrendChanged(
                                AuctionObservation.PriceTrend.RISING
                            )
                        )
                    },
                    label = { Text("上涨") }
                )
                FilterChip(
                    selected = uiState.priceTrend == AuctionObservation.PriceTrend.FLAT,
                    onClick = {
                        viewModel.handleEvent(
                            AuctionObservationEvent.PriceTrendChanged(
                                AuctionObservation.PriceTrend.FLAT
                            )
                        )
                    },
                    label = { Text("横盘") }
                )
                FilterChip(
                    selected = uiState.priceTrend == AuctionObservation.PriceTrend.FALLING,
                    onClick = {
                        viewModel.handleEvent(
                            AuctionObservationEvent.PriceTrendChanged(
                                AuctionObservation.PriceTrend.FALLING
                            )
                        )
                    },
                    label = { Text("下跌") }
                )
            }
            
            // 关键观察点
            Text(
                text = "关键观察点",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            for ((index, observation) in uiState.keyObservations.withIndex()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = observation,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                viewModel.handleEvent(AuctionObservationEvent.RemoveKeyObservation(index))
                            }
                        ) {
                            Text("删除")
                        }
                    }
                }
            }
            
            var newObservation by remember { mutableStateOf("") }
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                OutlinedTextField(
                    value = newObservation,
                    onValueChange = { newObservation = it },
                    label = { Text("添加观察点") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (newObservation.isNotBlank()) {
                            viewModel.handleEvent(AuctionObservationEvent.AddKeyObservation(newObservation))
                            newObservation = ""
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("添加")
                }
            }
            
            // 成交量分析
            OutlinedTextField(
                value = uiState.volumeAnalysis,
                onValueChange = {
                    viewModel.handleEvent(AuctionObservationEvent.VolumeAnalysisChanged(it))
                },
                label = { Text("成交量分析") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                minLines = 2,
                maxLines = 4
            )
            
            // 备注
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = {
                    viewModel.handleEvent(AuctionObservationEvent.NotesChanged(it))
                },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                minLines = 2,
                maxLines = 4
            )
        }
    }
}
