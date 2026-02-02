package com.alphadoer.trader.presentation.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphadoer.trader.presentation.home.HomeUiState
import com.alphadoer.trader.presentation.viewmodel.home.HomeViewModel
import com.alphadoer.trader.presentation.navigation.Screen

/**
 * 主界面
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToStep: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 显示错误消息
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                Snackbar(snackbarData = snackbarData)
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            uiState.process?.let { process ->
                ProcessOverview(
                    process = process,
                    onStepClick = { stepId ->
                        viewModel.startStep(stepId)
                        process.steps.find { it.id == stepId }?.let { step ->
                            onNavigateToStep(step.route)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // 首页快捷入口：强势板块记录
            Button(
                onClick = { onNavigateToStep(Screen.SectorTracking.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                androidx.compose.material3.Text("记录强势板块（今日）")
            }

            // 首页快捷入口：查看盘后总结
            Button(
                onClick = { onNavigateToStep(Screen.ReviewSummary.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                androidx.compose.material3.Text("查看盘后总结与建议")
            }
        }
    }
}
