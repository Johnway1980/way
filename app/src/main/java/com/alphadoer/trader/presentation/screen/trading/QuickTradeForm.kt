package com.alphadoer.trader.presentation.screen.trading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alphadoer.trader.domain.model.trading.TradeOperation
import com.alphadoer.trader.domain.model.trading.TradeRecord
import com.alphadoer.trader.domain.model.trading.TradeStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 快速交易记录表单
 */
@Composable
fun QuickTradeForm(
    onRecordTrade: (TradeRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    var stockCode by remember { mutableStateOf("") }
    var stockName by remember { mutableStateOf("") }
    var operation by remember { mutableStateOf(TradeOperation.BUY) }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = dateFormat.format(Date())
    
    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "快速记录交易",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // 股票代码
            OutlinedTextField(
                value = stockCode,
                onValueChange = { stockCode = it },
                label = { Text("股票代码") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("如：000001") }
            )
            
            // 股票名称
            OutlinedTextField(
                value = stockName,
                onValueChange = { stockName = it },
                label = { Text("股票名称") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("如：平安银行") }
            )
            
            // 操作类型
            Text(
                text = "操作类型",
                style = MaterialTheme.typography.titleMedium
            )
            Column {
                TradeOperation.values().forEach { op ->
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = operation == op,
                            onClick = { operation = op },
                            colors = RadioButtonDefaults.colors()
                        )
                        Text(
                            text = when (op) {
                                TradeOperation.BUY -> "买入"
                                TradeOperation.SELL -> "卖出"
                                TradeOperation.HOLD -> "持有"
                                TradeOperation.CANCEL -> "取消"
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            
            // 价格
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("价格") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("如：10.50") }
            )
            
            // 数量
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("数量") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("如：100") }
            )
            
            // 交易理由
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("交易理由（可选）") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                minLines = 3,
                maxLines = 5,
                placeholder = { Text("简要说明交易理由...") }
            )
            
            // 提交按钮
            Button(
                onClick = {
                    val record = TradeRecord(
                        date = today,
                        stockCode = stockCode.trim(),
                        stockName = stockName.trim(),
                        operation = operation,
                        status = TradeStatus.EXECUTED,
                        price = price.toDoubleOrNull() ?: 0.0,
                        quantity = quantity.toIntOrNull() ?: 0,
                        amount = 0.0,
                        reason = reason.takeIf { it.isNotBlank() }
                    )
                    onRecordTrade(record)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = stockCode.isNotBlank() && 
                         stockName.isNotBlank() && 
                         price.toDoubleOrNull() != null && 
                         price.toDoubleOrNull()!! > 0 &&
                         quantity.toIntOrNull() != null && 
                         quantity.toIntOrNull()!! > 0
            ) {
                Text("记录交易")
            }
        }
    }
}
