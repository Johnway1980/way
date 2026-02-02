package com.alphadoer.trader.domain.model.settings

import com.squareup.moshi.JsonClass

/**
 * 外观设置
 */
@JsonClass(generateAdapter = true)
data class AppearanceSettings(
    val themeMode: ThemeMode,
    val fontSize: FontSize,
    val colorScheme: String? = null, // 自定义色彩方案ID
    val animationEnabled: Boolean = true, // 动画效果
    val updatedAt: Long = System.currentTimeMillis()
) {
    enum class ThemeMode {
        LIGHT,      // 浅色
        DARK,       // 深色
        AUTO        // 跟随系统
    }
    
    enum class FontSize {
        SMALL,      // 小
        MEDIUM,     // 中
        LARGE,      // 大
        EXTRA_LARGE // 特大
    }
}
