package com.alphadoer.trader.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController? = null) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (navController != null) {
            Button(
                onClick = {
                    navController.navigate("morning_reading")
                }
            ) {
                Text("进入早间信息阅读")
            }
        } else {
            CenteredText("首页（交易日志）")
        }
    }
}

@Composable
fun HistoryScreen() {
    CenteredText("历史记录")
}

@Composable
fun StatisticsScreen() {
    CenteredText("统计分析")
}

@Composable
fun SettingsScreen() {
    CenteredText("设置")
}

@Composable
fun CenteredText(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text)
    }
}

