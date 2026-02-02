package com.alphadoer.trader.presentation.screen.morningreading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alphadoer.trader.domain.model.NewsAnalysis
import com.alphadoer.trader.presentation.morningreading.LoadingState

/**
 * 分析结果展示面板
 */
@Composable
fun AnalysisResultPanel(
    analysis: NewsAnalysis?,
    loadingState: LoadingState,
    onApplyToPlan: (String) -> Unit,
    onLinkToSectorRecords: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        when {
            loadingState == LoadingState.LOADING -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "AI分析中...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            analysis == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "分析结果将在此显示",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            else -> {
                AnalysisResultContent(
                    analysis = analysis,
                    onApplyToPlan = onApplyToPlan,
                    onLinkToSectorRecords = onLinkToSectorRecords
                )
            }
        }
    }
}

@Composable
private fun AnalysisResultContent(
    analysis: NewsAnalysis,
    onApplyToPlan: (String) -> Unit,
    onLinkToSectorRecords: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "分析结果",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // 重点板块TOP2及每板块3股
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    text = "重点板块TOP2与推荐",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val topSectors = analysis.affectedSectors.take(2)
                val bySector = analysis.recommendedStocks.groupBy { it.sectorName ?: "未标明" }
                for (sector in topSectors) {
                    val picks = (bySector[sector.sectorName] ?: emptyList()).take(3)
                    Text(
                        text = "【${sector.sectorName}】影响程度：${when (sector.impactLevel) { com.alphadoer.trader.domain.model.AffectedSector.ImpactLevel.HIGH -> "高"; com.alphadoer.trader.domain.model.AffectedSector.ImpactLevel.MEDIUM -> "中"; else -> "低" }}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    picks.forEach { s ->
                        Text(
                            text = "- ${s.stockName}（${s.stockCode}）：${s.reason}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (topSectors.isEmpty()) {
                    Text(
                        text = "暂无重点板块",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // 摘要部分
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            SummaryTab(analysis = analysis)
        }
        
        // 影响板块部分
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            AffectedSectorsTab(analysis = analysis)
        }
        
        // 关注个股部分
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            RecommendedStocksTab(
                analysis = analysis,
                onApplyToPlan = onApplyToPlan
            )
        }
        
        // 风险提示部分
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            RiskWarningsTab(analysis = analysis)
        }
        
        // 应用到计划按钮
        Button(
            onClick = { onApplyToPlan(analysis.id) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("应用到今日计划")
        }

        // 加入强势板块记录（TOP3自动补齐至≥5股）
        Button(
            onClick = { onLinkToSectorRecords(analysis.id) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("加入强势板块记录（TOP3，≥5股自动补齐）")
        }
    }
}

@Composable
private fun SummaryTab(analysis: NewsAnalysis) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "核心摘要",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = analysis.summary,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            text = "情绪分析",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "情绪: ${analysis.sentiment.name} | 置信度: ${(analysis.confidence * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium
        )
        
        if (analysis.keyPoints.isNotEmpty()) {
            Text(
                text = "关键要点",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
            for ((index, point) in analysis.keyPoints.withIndex()) {
                Text(
                    text = "${index + 1}. $point",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AffectedSectorsTab(analysis: NewsAnalysis) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "影响板块",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        if (analysis.affectedSectors.isEmpty()) {
            Text(
                text = "暂无板块影响分析",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            for (sector in analysis.affectedSectors) {
                SectorCard(sector = sector)
            }
        }
    }
}

@Composable
private fun RecommendedStocksTab(
    analysis: NewsAnalysis,
    onApplyToPlan: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "关注个股",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        if (analysis.recommendedStocks.isEmpty()) {
            Text(
                text = "暂无推荐股票",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            for (stock in analysis.recommendedStocks) {
                StockCard(
                    stock = stock,
                    onApplyToPlan = { onApplyToPlan(analysis.id) }
                )
            }
        }
    }
}

@Composable
private fun RiskWarningsTab(analysis: NewsAnalysis) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "风险提示",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        if (analysis.riskWarnings.isEmpty()) {
            Text(
                text = "暂无风险提示",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            for (warning in analysis.riskWarnings) {
                RiskWarningCard(warning = warning)
            }
        }
    }
}

@Composable
private fun SectorCard(sector: com.alphadoer.trader.domain.model.AffectedSector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                text = sector.sectorName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "影响程度: ${when (sector.impactLevel) {
                    com.alphadoer.trader.domain.model.AffectedSector.ImpactLevel.HIGH -> "高"
                    com.alphadoer.trader.domain.model.AffectedSector.ImpactLevel.MEDIUM -> "中"
                    com.alphadoer.trader.domain.model.AffectedSector.ImpactLevel.LOW -> "低"
                }}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = sector.impactDescription,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun StockCard(
    stock: com.alphadoer.trader.domain.model.RecommendedStock,
    onApplyToPlan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                        text = "${stock.stockName} (${stock.stockCode})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "建议: ${stock.recommendation.name} | 置信度: ${(stock.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    stock.sectorName?.let { sector ->
                        Text(
                            text = "所属板块: $sector",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                IconButton(onClick = onApplyToPlan) {
                    androidx.compose.material.icons.Icons.Default.Add
                }
            }
            Text(
                text = stock.reason,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun RiskWarningCard(warning: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = warning,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )
    }
}
