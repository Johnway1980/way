package com.alphadoer.trader.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.alphadoer.trader.data.local.converters.ListStringConverter

/**
 * 股票基础信息表
 */
@Entity(tableName = "stock")
@TypeConverters(ListStringConverter::class)
data class StockEntity(
    @PrimaryKey
    val code: String, // 股票代码（如：000001）
    val name: String, // 股票名称
    val market: String, // 市场: "SH" | "SZ" | "BJ"
    val sector: String? = null, // 所属板块
    val industry: String? = null, // 所属行业
    val tags: List<String>? = null, // 标签列表
    val isFavorite: Boolean = false, // 是否收藏
    val notes: String? = null, // 备注
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) {
    companion object {
        fun create(
            code: String,
            name: String,
            market: String,
            sector: String? = null,
            industry: String? = null,
            tags: List<String>? = null,
            isFavorite: Boolean = false,
            notes: String? = null
        ): StockEntity {
            val now = System.currentTimeMillis()
            return StockEntity(
                code = code,
                name = name,
                market = market,
                sector = sector,
                industry = industry,
                tags = tags,
                isFavorite = isFavorite,
                notes = notes,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
