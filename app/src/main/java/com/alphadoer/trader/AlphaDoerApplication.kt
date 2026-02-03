package com.alphadoer.trader

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AlphaDoerApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    @Inject
    lateinit var processRepository: com.alphadoer.trader.domain.repository.ProcessRepository

    @Inject
    lateinit var settingsRepository: com.alphadoer.trader.domain.repository.SettingsRepository
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化步骤配置
        applicationScope.launch {
            processRepository.initializeStepConfigs()
        }

        // 启动时调度提醒（基于当前通知偏好）
        applicationScope.launch {
            val pref = settingsRepository.getNotificationPreference()
                ?: com.alphadoer.trader.domain.model.settings.NotificationPreference()
            com.alphadoer.trader.data.util.ReminderScheduler.scheduleAll(this@AlphaDoerApplication, pref)
        }

        // 启动时应用AI分析校准参数
        applicationScope.launch {
            val fs = settingsRepository.getFunctionalSettings()
                ?: com.alphadoer.trader.domain.model.settings.FunctionalSettings()
            com.alphadoer.trader.data.util.StockValidationTuning.strictNullDomainMismatch = fs.analysisStrictNullDomain
            com.alphadoer.trader.data.util.StockValidationTuning.minReasonLength = fs.analysisMinReasonLength
            com.alphadoer.trader.data.util.StockValidationTuning.enableSamplingLog = fs.analysisSamplingEnabled
            com.alphadoer.trader.data.util.StockValidationTuning.samplingRatio = fs.analysisSamplingRatio
        }
    }
}

