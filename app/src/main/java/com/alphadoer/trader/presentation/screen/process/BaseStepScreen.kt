package com.alphadoer.trader.presentation.screen.process

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 步骤基础界面模板
 */
@Composable
fun BaseStepScreen(
    stepName: String,
    stepDescription: String,
    content: @Composable () -> Unit,
    onComplete: () -> Unit,
    onSkip: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 步骤标题
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stepName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stepDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // 步骤内容
            content()
            
            // 操作按钮
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("完成步骤")
                }
                
                if (onSkip != null) {
                    androidx.compose.material3.TextButton(
                        onClick = onSkip,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("跳过")
                    }
                }
            }
        }
    }
}
