package com.alphadoer.trader.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.alphadoer.trader.data.local.converters.LocalDateTimeConverter
import com.alphadoer.trader.data.local.converters.MapStringStringConverter

/**
 * 错误模式记录表
 */
@Entity(
    tableName = "mistake_pattern",
    foreignKeys = [
        ForeignKey(
            entity = TradeJournalEntity::class,
            parentColumns = ["date"],
            childColumns = ["journalDate"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["journalDate"]), Index(value = ["patternType"])]
)
@TypeConverters(LocalDateTimeConverter::class, MapStringStringConverter::class)
data class MistakePatternEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val journalDate: String, // 关联的交易日报日期
    val patternType: String, // 错误模式类型: "OVERTRADING" | "EMOTIONAL" | "TIMING" 等
    val description: String, // 错误描述
    val severity: Int, // 严重程度 1-5
    val impact: String? = null, // 影响说明
    val lesson: String? = null, // 经验教训
    val metadata: Map<String, String>? = null, // 额外元数据
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) {
    companion object {
        fun create(
            journalDate: String,
            patternType: String,
            description: String,
            severity: Int,
            impact: String? = null,
            lesson: String? = null,
            metadata: Map<String, String>? = null
        ): MistakePatternEntity {
            val now = System.currentTimeMillis()
            return MistakePatternEntity(
                journalDate = journalDate,
                patternType = patternType,
                description = description,
                severity = severity,
                impact = impact,
                lesson = lesson,
                metadata = metadata,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
