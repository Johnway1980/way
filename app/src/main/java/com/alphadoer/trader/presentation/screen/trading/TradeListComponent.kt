package com.alphadoer.trader.presentation.screen.trading

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alphadoer.trader.domain.model.trading.TradeOperation
import com.alphadoer.trader.domain.model.trading.TradeRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 交易记录列表组件
 */
@Composable
fun TradeListComponent(
    trades: List<TradeRecord>,
    onTradeClick: (TradeRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    if (trades.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "暂无交易记录",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            items(trades) { trade ->
                TradeItemCard(
                    trade = trade,
                    onClick = { onTradeClick(trade) }
                )
            }
        }
    }
}

@Composable
private fun TradeItemCard(
    trade: TradeRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = when (trade.operation) {
                TradeOperation.BUY -> MaterialTheme.colorScheme.primaryContainer
                TradeOperation.SELL -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${trade.stockName} (${trade.stockCode})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = when (trade.operation) {
                            TradeOperation.BUY -> "买入"
                            TradeOperation.SELL -> "卖出"
                            TradeOperation.HOLD -> "持有"
                            TradeOperation.CANCEL -> "取消"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Text(
                    text = "¥${String.format("%.2f", trade.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "价格: ¥${String.format("%.2f", trade.price)} × ${trade.quantity}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        .format(Date(trade.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            trade.profitLoss?.let { profitLoss ->
                Text(
                    text = if (profitLoss >= 0) {
                        "盈亏: +¥${String.format("%.2f", profitLoss)}"
                    } else {
                        "盈亏: -¥${String.format("%.2f", -profitLoss)}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (profitLoss >= 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
