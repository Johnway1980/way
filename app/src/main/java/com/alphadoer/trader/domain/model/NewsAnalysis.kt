package com.alphadoer.trader.domain.model

import com.squareup.moshi.JsonClass

/**
 * AI分析结果领域模型
 */
@JsonClass(generateAdapter = true)
data class NewsAnalysis(
    val id: String,
    val newsContent: String,
    val summary: String,
    val sentiment: Sentiment,
    val confidence: Double, // 0.0-1.0
    val keyPoints: List<String>,
    val affectedSectors: List<AffectedSector>,
    val recommendedStocks: List<RecommendedStock>,
    val riskWarnings: List<String>,
    val recommendations: List<String>,
    val analysisType: AnalysisType,
    val createdAt: Long,
    val metadata: Map<String, String>? = null
) {
    enum class Sentiment {
        POSITIVE,    // 积极
        NEGATIVE,    // 消极
        NEUTRAL      // 中性
    }
    
    enum class AnalysisType {
        QUICK,       // 快速分析
        DEEP         // 深度分析
    }
}
