package com.alphadoer.trader.domain.model

import com.squareup.moshi.JsonClass

/**
 * 当日板块选择记录
 */
@JsonClass(generateAdapter = true)
data class SectorSelectionRecord(
    val id: String,
    val date: String, // yyyy-MM-dd
    val sectorCode: String,
    val sectorName: String,
    val stockCodes: List<String>, // 至少5只
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
