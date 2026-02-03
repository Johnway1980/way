package com.alphadoer.trader.presentation.screen.home

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alphadoer.trader.domain.model.process.DailyProcess
import com.alphadoer.trader.domain.model.process.StepStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 流程概览组件
 */
@Composable
fun ProcessOverview(
    process: DailyProcess,
    onStepClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 日期和总体进度
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "日期: ${process.date}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // 总体进度环
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    progress = { process.overallProgress.toFloat() },
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = "${(process.overallProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // 步骤列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(process.steps) { step ->
                val stepState = process.getStepState(step.id)
                StepStatusCard(
                    step = step,
                    stepState = stepState,
                    onClick = { onStepClick(step.id) }
                )
            }
        }
    }
}

/**
 * 步骤状态卡片
 */
@Composable
fun StepStatusCard(
    step: com.alphadoer.trader.domain.model.process.ProcessStep,
    stepState: com.alphadoer.trader.domain.model.process.StepState?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = when (stepState?.status) {
                StepStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                StepStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer
                StepStatus.SKIPPED -> MaterialTheme.colorScheme.surfaceVariant
                StepStatus.BLOCKED -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${step.type.stepNumber}. ${step.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // 状态指示器
                Text(
                    text = when (stepState?.status) {
                        StepStatus.COMPLETED -> "✓"
                        StepStatus.IN_PROGRESS -> "→"
                        StepStatus.SKIPPED -> "⊘"
                        StepStatus.BLOCKED -> "⊘"
                        else -> "○"
                    },
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            // 时间信息
            stepState?.let { state ->
                if (state.startedAt != null) {
                    Text(
                        text = "开始: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(state.startedAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                if (state.completedAt != null) {
                    Text(
                        text = "完成: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(state.completedAt))}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
