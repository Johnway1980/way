package com.alphadoer.trader.presentation.screen.trading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alphadoer.trader.domain.model.trading.TradeStatistics

/**
 * 交易统计面板
 */
@Composable
fun TradingStatsPanel(
    statistics: TradeStatistics?,
    modifier: Modifier = Modifier
) {
    if (statistics == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "暂无统计数据",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 总体统计
            StatCard(
                title = "当日交易统计",
                items = listOf(
                    "交易次数" to statistics.totalTrades.toString(),
                    "买入次数" to statistics.buyCount.toString(),
                    "卖出次数" to statistics.sellCount.toString(),
                    "总盈亏" to String.format("%.2f", statistics.totalProfitLoss),
                    "胜率" to String.format("%.1f%%", statistics.winRate)
                )
            )
            
            // 持仓概览
            if (statistics.positions.isNotEmpty()) {
                StatCard(
                    title = "持仓概览",
                    items = statistics.positions.map { position ->
                        "${position.stockName} (${position.stockCode})" to "${position.quantity}股"
                    }
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
