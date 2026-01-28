package com.alphadoer.trader.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.alphadoer.trader.data.local.converters.LocalDateTimeConverter

/**
 * 交易日报主表
 */
@Entity(tableName = "trade_journal")
@TypeConverters(LocalDateTimeConverter::class)
data class TradeJournalEntity(
    @PrimaryKey
    val date: String, // yyyy-MM-dd 格式，作为主键
    val morningConclusion: String? = null, // 早间总结
    val auctionFeeling: Int? = null, // 集合竞价感受 1-5
    val reviewCompleted: Boolean = false, // 是否完成复盘
    val createdAt: Long? = null, // 创建时间戳
    val updatedAt: Long? = null // 更新时间戳
) {
    companion object {
        fun create(
            date: String,
            morningConclusion: String? = null,
            auctionFeeling: Int? = null,
            reviewCompleted: Boolean = false
        ): TradeJournalEntity {
            val now = System.currentTimeMillis()
            return TradeJournalEntity(
                date = date,
                morningConclusion = morningConclusion,
                auctionFeeling = auctionFeeling,
                reviewCompleted = reviewCompleted,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
