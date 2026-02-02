package com.alphadoer.trader.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.alphadoer.trader.data.local.converters.MapStringStringConverter

/**
 * 交易错误实体
 */
@Entity(tableName = "trade_mistakes")
@TypeConverters(MapStringStringConverter::class)
data class TradeMistakeEntity(
    @PrimaryKey
    val id: String,
    val tradeRecordId: String,
    val date: String, // yyyy-MM-dd
    val mistakeType: String, // TradeMistake.MistakeType.name
    val category: String, // TradeMistake.MistakeCategory.name
    val description: String,
    val rootCause: String,
    val impactAmount: Double,
    val contextJson: String, // JSON格式的MistakeContext
    val improvementMeasuresJson: String, // JSON格式的List<String>
    val relatedMistakesJson: String, // JSON格式的List<String>
    val createdAt: Long
)
