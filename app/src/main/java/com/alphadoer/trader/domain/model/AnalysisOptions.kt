package com.alphadoer.trader.domain.model

/**
 * 分析选项
 */
data class AnalysisOptions(
    val analysisType: NewsAnalysis.AnalysisType = NewsAnalysis.AnalysisType.QUICK,
    val maxRecommendedStocks: Int = 5,
    val includeRiskAnalysis: Boolean = true,
    val includeSectorAnalysis: Boolean = true,
    val focusSectors: List<String>? = null, // 关注的板块代码列表
    val excludeStocks: List<String>? = null // 排除的股票代码列表
)
