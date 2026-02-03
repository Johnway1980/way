package com.alphadoer.trader.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * AI分析请求DTO
 */
@JsonClass(generateAdapter = true)
data class AIAnalysisRequest(
    @Json(name = "content")
    val content: String,
    
    @Json(name = "analysis_type")
    val analysisType: String, // "NEWS" | "MARKET" | "STOCK"
    
    @Json(name = "context")
    val context: Map<String, String>? = null,
    
    @Json(name = "options")
    val options: Map<String, String>? = null
)
