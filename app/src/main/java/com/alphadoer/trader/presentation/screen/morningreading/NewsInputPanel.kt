package com.alphadoer.trader.presentation.screen.morningreading

import com.alphadoer.trader.presentation.morningreading.NewsTemplate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 新闻输入面板
 */
@Composable
fun NewsInputPanel(
    newsText: String,
    onNewsTextChanged: (String) -> Unit,
    onUseTemplate: (NewsTemplate) -> Unit,
    onNewsSourceChanged: (String) -> Unit,
    onNewsUrlChanged: (String) -> Unit,
    onNewsTimeChanged: (String) -> Unit,
    onNewsTagsChanged: (String) -> Unit,
    newsSource: String,
    newsUrl: String,
    newsTime: String,
    newsTagsCsv: String,
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
                text = "新闻内容输入",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // 模板按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (template in NewsTemplate.values()) {
                    OutlinedButton(
                        onClick = { onUseTemplate(template) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = when (template) {
                                NewsTemplate.INTERNATIONAL -> "国际新闻"
                                NewsTemplate.DOMESTIC_POLICY -> "国内政策"
                                NewsTemplate.INDUSTRY -> "行业动态"
                                NewsTemplate.COMPANY -> "公司公告"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // 新闻输入框
            TextField(
                value = newsText,
                onValueChange = { if (it.length <= 5000) onNewsTextChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("请输入或粘贴新闻内容...") },
                minLines = 8,
                maxLines = 15
            )
            
            // 字数统计
            Text(
                text = "字数: ${newsText.length} / 5000",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 额外信息
            TextField(
                value = newsSource,
                onValueChange = onNewsSourceChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("来源（媒体/作者）") }
            )
            TextField(
                value = newsUrl,
                onValueChange = onNewsUrlChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("链接（可选）") }
            )
            TextField(
                value = newsTime,
                onValueChange = onNewsTimeChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("时间（yyyy-MM-dd HH:mm，可选）") }
            )
            TextField(
                value = newsTagsCsv,
                onValueChange = onNewsTagsChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("标签（以逗号分隔，例如：AI,政策,行业）") }
            )
        }
    }
}
