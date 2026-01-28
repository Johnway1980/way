package com.alphadoer.trader.presentation.screen.morningreading

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alphadoer.trader.domain.model.NewsAnalysis
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 分析控制面板
 */
@Composable
fun AnalysisControlPanel(
    canAnalyze: Boolean,
    isLoading: Boolean,
    analysisHistory: List<NewsAnalysis>,
    onAnalyzeClick: () -> Unit,
    onHistoryItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "分析控制",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // 分析按钮
            Button(
                onClick = onAnalyzeClick,
                enabled = canAnalyze && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = when {
                        isLoading -> "分析中..."
                        canAnalyze -> "开始分析"
                        else -> "请输入新闻内容"
                    }
                )
            }
            
            // 分析历史
            Text(
                text = "分析历史",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            if (analysisHistory.isEmpty()) {
                Text(
                    text = "暂无分析历史",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (analysis in analysisHistory.take(5)) {
                        AnalysisHistoryItem(
                            analysis = analysis,
                            onClick = { onHistoryItemClick(analysis.id) }
                        )
                    }
                    if (analysisHistory.size > 5) {
                        Text(
                            text = "还有 ${analysisHistory.size - 5} 条历史记录...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisHistoryItem(
    analysis: NewsAnalysis,
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
                text = analysis.summary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                    .format(Date(analysis.createdAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
