package com.alphadoer.trader.domain.model

import com.squareup.moshi.JsonClass

/**
 * 受影响板块
 */
@JsonClass(generateAdapter = true)
data class AffectedSector(
    val sectorCode: String,
    val sectorName: String,
    val impactLevel: ImpactLevel,
    val impactDescription: String,
    val relatedStocks: List<String> // 股票代码列表
) {
    enum class ImpactLevel {
        HIGH,      // 高影响
        MEDIUM,    // 中等影响
        LOW        // 低影响
    }
}
