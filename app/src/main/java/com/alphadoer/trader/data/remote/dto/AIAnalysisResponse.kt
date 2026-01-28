package com.alphadoer.trader.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * AI分析响应DTO
 */
@JsonClass(generateAdapter = true)
data class AIAnalysisResponse(
    @Json(name = "success")
    val success: Boolean,
    
    @Json(name = "data")
    val data: AnalysisData? = null,
    
    @Json(name = "error")
    val error: ErrorInfo? = null
)

@JsonClass(generateAdapter = true)
data class AnalysisData(
    @Json(name = "summary")
    val summary: String,
    
    @Json(name = "key_points")
    val keyPoints: List<String>? = null,
    
    @Json(name = "sentiment")
    val sentiment: String? = null, // "POSITIVE" | "NEGATIVE" | "NEUTRAL"
    
    @Json(name = "confidence")
    val confidence: Double? = null, // 0.0-1.0
    
    @Json(name = "recommendations")
    val recommendations: List<String>? = null,
    
    @Json(name = "metadata")
    val metadata: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class ErrorInfo(
    @Json(name = "code")
    val code: String,
    
    @Json(name = "message")
    val message: String,
    
    @Json(name = "details")
    val details: Map<String, String>? = null
)
