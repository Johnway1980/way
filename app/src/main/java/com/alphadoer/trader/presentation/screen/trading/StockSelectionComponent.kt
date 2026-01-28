package com.alphadoer.trader.presentation.screen.trading

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alphadoer.trader.domain.model.RecommendedStock

/**
 * 股票选择组件
 */
@Composable
fun StockSelectionComponent(
    recommendedStocks: List<RecommendedStock> = emptyList(),
    onStockSelected: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredStocks = if (searchQuery.isBlank()) {
        recommendedStocks
    } else {
        recommendedStocks.filter {
            it.stockCode.contains(searchQuery, ignoreCase = true) ||
            it.stockName.contains(searchQuery, ignoreCase = true)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "选择股票",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("搜索股票代码或名称") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("输入代码或名称...") }
        )
        
        if (filteredStocks.isEmpty()) {
            Text(
                text = if (searchQuery.isBlank()) "暂无推荐股票" else "未找到匹配的股票",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredStocks) { stock ->
                    StockItemCard(
                        stock = stock,
                        onClick = { onStockSelected(stock.stockCode, stock.stockName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StockItemCard(
    stock: RecommendedStock,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "${stock.stockName} (${stock.stockCode})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stock.reason,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
