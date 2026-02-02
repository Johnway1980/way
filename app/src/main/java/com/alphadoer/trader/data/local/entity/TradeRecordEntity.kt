package com.alphadoer.trader.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.alphadoer.trader.data.local.converters.LocalDateTimeConverter

/**
 * 交易执行记录表
 */
@Entity(
    tableName = "trade_record",
    foreignKeys = [
        ForeignKey(
            entity = TradeJournalEntity::class,
            parentColumns = ["date"],
            childColumns = ["journalDate"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["journalDate"])]
)
@TypeConverters(LocalDateTimeConverter::class)
data class TradeRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val journalDate: String, // 关联的交易日报日期
    val stockCode: String, // 股票代码
    val stockName: String, // 股票名称
    val tradeType: String, // 交易类型: "BUY" | "SELL"
    val price: Double, // 成交价格
    val quantity: Int, // 成交数量
    val tradeTime: Long, // 交易时间戳
    val profit: Double? = null, // 盈亏金额（卖出时计算）
    val profitRate: Double? = null, // 盈亏比例（卖出时计算）
    val notes: String? = null, // 备注
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) {
    companion object {
        fun create(
            journalDate: String,
            stockCode: String,
            stockName: String,
            tradeType: String,
            price: Double,
            quantity: Int,
            tradeTime: Long,
            profit: Double? = null,
            profitRate: Double? = null,
            notes: String? = null
        ): TradeRecordEntity {
            val now = System.currentTimeMillis()
            return TradeRecordEntity(
                journalDate = journalDate,
                stockCode = stockCode,
                stockName = stockName,
                tradeType = tradeType,
                price = price,
                quantity = quantity,
                tradeTime = tradeTime,
                profit = profit,
                profitRate = profitRate,
                notes = notes,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
