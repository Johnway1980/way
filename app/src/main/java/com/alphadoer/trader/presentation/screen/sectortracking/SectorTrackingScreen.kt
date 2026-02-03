package com.alphadoer.trader.presentation.screen.sectortracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphadoer.trader.presentation.viewmodel.sectortracking.SectorTrackingViewModel

@Composable
fun SectorTrackingScreen(
    viewModel: SectorTrackingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data -> Snackbar(snackbarData = data) }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "强势板块记录（至少3个板块，每板块≥5股）",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "日期：${uiState.date}", style = MaterialTheme.typography.bodyMedium)
                    TextField(value = uiState.inputSectorName, onValueChange = viewModel::updateSectorName, modifier = Modifier.fillMaxWidth(), placeholder = { Text("板块名称，如：半导体") })
                    TextField(value = uiState.inputSectorCode, onValueChange = viewModel::updateSectorCode, modifier = Modifier.fillMaxWidth(), placeholder = { Text("板块代码，可自定义，如：semiconductor") })
                    TextField(value = uiState.inputStockCodesCsv, onValueChange = viewModel::updateStockCodesCsv, modifier = Modifier.fillMaxWidth(), placeholder = { Text("股票代码或名称，以逗号分隔，至少5个") })
                    Button(onClick = { viewModel.saveSelection() }, modifier = Modifier.fillMaxWidth()) { Text("保存板块记录") }
                }
            }

            // 已记录列表
            uiState.selections.forEach { rec ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "${rec.sectorName}（${rec.sectorCode}）", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(text = "股票：${rec.stockCodes.joinToString()}", style = MaterialTheme.typography.bodyMedium)
                        Button(onClick = { viewModel.deleteSelection(rec.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text("删除该记录")
                        }
                    }
                }
            }
        }
    }
}
