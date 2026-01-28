package com.alphadoer.trader.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.alphadoer.trader.data.local.converters.ListStringConverter

/**
 * 板块信息表
 */
@Entity(tableName = "sector")
@TypeConverters(ListStringConverter::class)
data class SectorEntity(
    @PrimaryKey
    val code: String, // 板块代码
    val name: String, // 板块名称
    val type: String, // 板块类型: "INDUSTRY" | "CONCEPT" | "REGION" 等
    val description: String? = null, // 板块描述
    val stockCodes: List<String>? = null, // 包含的股票代码列表
    val isActive: Boolean = true, // 是否活跃
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) {
    companion object {
        fun create(
            code: String,
            name: String,
            type: String,
            description: String? = null,
            stockCodes: List<String>? = null,
            isActive: Boolean = true
        ): SectorEntity {
            val now = System.currentTimeMillis()
            return SectorEntity(
                code = code,
                name = name,
                type = type,
                description = description,
                stockCodes = stockCodes,
                isActive = isActive,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
