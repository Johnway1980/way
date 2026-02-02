package com.alphadoer.trader.domain.model

/**
 * 交易日志核心领域实体
 */
data class TradeJournal(
    val date: String, // yyyy-MM-dd
    val morningConclusion: String?,
    val auctionFeeling: Int?, // 1-5
    val reviewCompleted: Boolean
)

