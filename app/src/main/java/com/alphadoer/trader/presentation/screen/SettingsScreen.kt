package com.alphadoer.trader.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphadoer.trader.presentation.viewmodel.settings.SettingsViewModel
import kotlinx.coroutines.launch
import com.alphadoer.trader.presentation.settings.SettingsTuningSection

/**
 * 设置主界面
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateTo: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearError()
            }
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
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "加载中...")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    Text(
                        text = "设置",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                // 外观设置
                item {
                    SettingsSectionTitle("外观设置")
                }
                
                item {
                    val appearance = uiState.userSettings?.appearanceSettings
                    if (appearance != null) {
                        ThemeModeSetting(
                            currentMode = appearance.themeMode,
                            onModeChanged = { newMode ->
                                viewModel.updateAppearance(
                                    appearance.copy(themeMode = newMode)
                                )
                            }
                        )
                    }
                }
                
                // 交易偏好
                item {
                    SettingsSectionTitle("交易偏好")
                }
                
                item {
                    val tradingPref = uiState.userSettings?.tradingPreference
                    if (tradingPref != null) {
                        TradingStyleSetting(
                            currentStyle = tradingPref.tradingStyle,
                            onStyleChanged = { /* TODO: 实现 */ }
                        )
                    }
                }
                
                // 通知设置
                item {
                    SettingsSectionTitle("通知设置")
                }
                
                item {
                    val notificationPref = uiState.userSettings?.notificationPreference
                    if (notificationPref != null) {
                        NotificationSettingItem(
                            title = "交易提醒",
                            checked = notificationPref.tradeReminder,
                            onCheckedChange = { checked ->
                                val newPref = notificationPref.copy(tradeReminder = checked)
                                viewModel.updateNotificationPreference(newPref)
                                // 立即调度提醒
                                com.alphadoer.trader.data.util.ReminderScheduler.scheduleAll(context, newPref)
                            }
                        )
                        NotificationSettingItem(
                            title = "复盘提醒",
                            checked = notificationPref.reviewReminder,
                            onCheckedChange = { checked ->
                                val newPref = notificationPref.copy(reviewReminder = checked)
                                viewModel.updateNotificationPreference(newPref)
                                com.alphadoer.trader.data.util.ReminderScheduler.scheduleAll(context, newPref)
                            }
                        )
                    }
                }

                // AI分析校准
                item {
                    SettingsSectionTitle("AI分析规则校准")
                }
                item {
                    SettingsTuningSection()
                }
                
                // 数据管理
                item {
                    SettingsSectionTitle("数据管理")
                }
                
                item {
                    DataSettingItem(
                        title = "数据备份",
                        subtitle = "自动备份已开启",
                        onClick = { /* TODO: 实现 */ }
                    )
                }

                // 快捷入口
                item {
                    SettingsSectionTitle("快捷入口")
                }

                item {
                    DataSettingItem(
                        title = "记录强势板块（今日）",
                        subtitle = "快速打开当天板块记录界面",
                        onClick = { onNavigateTo(com.alphadoer.trader.presentation.navigation.Screen.SectorTracking.route) }
                    )
                }

                item {
                    DataSettingItem(
                        title = "查看盘后总结与建议",
                        subtitle = "基于新闻与记录的AI总结",
                        onClick = { onNavigateTo(com.alphadoer.trader.presentation.navigation.Screen.ReviewSummary.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ThemeModeSetting(
    currentMode: com.alphadoer.trader.domain.model.settings.AppearanceSettings.ThemeMode,
    onModeChanged: (com.alphadoer.trader.domain.model.settings.AppearanceSettings.ThemeMode) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "主题模式",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = when (currentMode) {
                    com.alphadoer.trader.domain.model.settings.AppearanceSettings.ThemeMode.LIGHT -> "浅色"
                    com.alphadoer.trader.domain.model.settings.AppearanceSettings.ThemeMode.DARK -> "深色"
                    com.alphadoer.trader.domain.model.settings.AppearanceSettings.ThemeMode.AUTO -> "跟随系统"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun TradingStyleSetting(
    currentStyle: com.alphadoer.trader.domain.model.settings.TradingPreference.TradingStyle,
    onStyleChanged: (com.alphadoer.trader.domain.model.settings.TradingPreference.TradingStyle) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "交易风格",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = when (currentStyle) {
                    com.alphadoer.trader.domain.model.settings.TradingPreference.TradingStyle.SHORT_TERM -> "短线交易"
                    com.alphadoer.trader.domain.model.settings.TradingPreference.TradingStyle.SWING -> "波段交易"
                    com.alphadoer.trader.domain.model.settings.TradingPreference.TradingStyle.LONG_TERM -> "长线投资"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun NotificationSettingItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun DataSettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
