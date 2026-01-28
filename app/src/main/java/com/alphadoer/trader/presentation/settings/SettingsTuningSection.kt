package com.alphadoer.trader.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphadoer.trader.presentation.viewmodel.settings.SettingsViewModel

/**
 * AI分析规则校准设置区块（Compose）。
 * 保持架构不变：直接调用 SettingsViewModel.updateAnalysisTuning。
 */
@Composable
fun SettingsTuningSection(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val ui = viewModel.uiState
    val fs = ui.value.userSettings?.functionalSettings

    var strict by remember { mutableStateOf(fs?.analysisStrictNullDomain ?: false) }
    var minLenText by remember { mutableStateOf((fs?.analysisMinReasonLength ?: 10).toString()) }
    var samplingEnabled by remember { mutableStateOf(fs?.analysisSamplingEnabled ?: true) }
    var samplingRatio by remember { mutableStateOf((fs?.analysisSamplingRatio ?: 0.1).toFloat()) }

    LaunchedEffect(fs) {
        fs?.let {
            strict = it.analysisStrictNullDomain
            minLenText = it.analysisMinReasonLength.toString()
            samplingEnabled = it.analysisSamplingEnabled
            samplingRatio = it.analysisSamplingRatio.toFloat()
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "AI分析规则校准")
        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "严格空领域不匹配")
            Switch(checked = strict, onCheckedChange = { strict = it })
        }
        Spacer(Modifier.height(8.dp))

        Text(text = "推荐理由最小长度")
        TextField(
            value = minLenText,
            onValueChange = { minLenText = it.filter { ch -> ch.isDigit() }.take(3) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "启用采样日志")
            Switch(checked = samplingEnabled, onCheckedChange = { samplingEnabled = it })
        }
        Spacer(Modifier.height(8.dp))

        Text(text = "采样比例：${String.format("%.2f", samplingRatio)}")
        Slider(
            value = samplingRatio,
            onValueChange = { samplingRatio = it.coerceIn(0f, 1f) },
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        Button(onClick = {
            val minLen = minLenText.toIntOrNull() ?: 10
            viewModel.updateAnalysisTuning(
                strictNullDomainMismatch = strict,
                minReasonLength = minLen.coerceIn(5, 200),
                samplingEnabled = samplingEnabled,
                samplingRatio = samplingRatio.toDouble()
            )
        }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "保存并应用")
        }
    }
}
