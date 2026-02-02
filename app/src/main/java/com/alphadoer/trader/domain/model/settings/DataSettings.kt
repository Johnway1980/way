package com.alphadoer.trader.domain.model.settings

import com.squareup.moshi.JsonClass

/**
 * 数据设置
 */
@JsonClass(generateAdapter = true)
data class DataSettings(
    val syncEnabled: Boolean = false, // 数据同步开关
    val syncFrequency: SyncFrequency = SyncFrequency.DAILY, // 同步频率
    val autoBackupEnabled: Boolean = true, // 自动备份
    val backupFrequency: BackupFrequency = BackupFrequency.WEEKLY, // 备份频率
    val dataRetentionDays: Int = 365, // 数据保留天数
    val cacheSizeLimit: Long = 500 * 1024 * 1024, // 缓存大小限制（字节），默认500MB
    val updatedAt: Long = System.currentTimeMillis()
) {
    enum class SyncFrequency {
        HOURLY,   // 每小时
        DAILY,    // 每天
        WEEKLY,   // 每周
        MANUAL    // 手动
    }
    
    enum class BackupFrequency {
        DAILY,    // 每天
        WEEKLY,   // 每周
        MONTHLY,  // 每月
        MANUAL    // 手动
    }
}
