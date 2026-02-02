package com.alphadoer.trader.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.alphadoer.trader.data.local.converters.LocalDateTimeConverter

/**
 * AI分析缓存表
 */
@Entity(tableName = "ai_analysis_cache")
@TypeConverters(LocalDateTimeConverter::class)
data class AIAnalysisCacheEntity(
    @PrimaryKey
    val cacheKey: String, // 缓存键（例如：新闻URL、日期等）
    val analysisType: String, // 分析类型: "NEWS" | "MARKET" | "STOCK" 等
    val content: String, // 分析内容（JSON格式）
    val summary: String? = null, // 摘要
    val confidence: Double? = null, // 置信度 0.0-1.0
    val expiresAt: Long? = null, // 过期时间戳
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) {
    companion object {
        fun create(
            cacheKey: String,
            analysisType: String,
            content: String,
            summary: String? = null,
            confidence: Double? = null,
            expiresAt: Long? = null
        ): AIAnalysisCacheEntity {
            val now = System.currentTimeMillis()
            return AIAnalysisCacheEntity(
                cacheKey = cacheKey,
                analysisType = analysisType,
                content = content,
                summary = summary,
                confidence = confidence,
                expiresAt = expiresAt,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
