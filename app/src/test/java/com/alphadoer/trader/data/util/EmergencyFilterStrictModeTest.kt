package com.alphadoer.trader.data.util

import com.alphadoer.trader.domain.model.AffectedSector
import com.alphadoer.trader.domain.model.NewsAnalysis
import com.alphadoer.trader.domain.model.RecommendedStock
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class EmergencyFilterStrictModeTest {

    @After
    fun tearDown() {
        // restore default
        EmergencyFilter.strictFiltering = true
    }

    private fun sampleAnalysis(): NewsAnalysis {
        val sectors = listOf(
            AffectedSector(
                sectorCode = "tech",
                sectorName = "科技",
                impactLevel = AffectedSector.ImpactLevel.HIGH,
                impactDescription = "test",
                relatedStocks = emptyList()
            )
        )

        val stocks = listOf(
            RecommendedStock(
                stockCode = "000001",
                stockName = "平安银行",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.BUY,
                reason = "test",
                confidence = 0.5,
                targetPrice = null,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "金融"
            ),
            RecommendedStock(
                stockCode = "300750",
                stockName = "宁德时代",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.BUY,
                reason = "test",
                confidence = 0.9,
                targetPrice = null,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "科技"
            )
        )

        return NewsAnalysis(
            id = "t1",
            newsContent = "AI 相关新闻",
            summary = "s",
            sentiment = NewsAnalysis.Sentiment.NEUTRAL,
            confidence = 0.5,
            keyPoints = emptyList(),
            affectedSectors = sectors,
            recommendedStocks = stocks,
            riskWarnings = emptyList(),
            recommendations = emptyList(),
            analysisType = NewsAnalysis.AnalysisType.QUICK,
            createdAt = System.currentTimeMillis(),
            metadata = null
        )
    }

    @Test
    fun `strictFiltering true should filter blacklisted stock`() {
        EmergencyFilter.strictFiltering = true
        val analysis = sampleAnalysis()
        val out = EmergencyFilter.filterAnalysisResult(analysis.newsContent, analysis)

        // 000001 is blacklisted -> should be filtered out
        assertFalse(out.recommendedStocks.any { it.stockCode == "000001" })
        // the tech stock should remain
        assertTrue(out.recommendedStocks.any { it.stockCode == "300750" })
    }

    @Test
    fun `strictFiltering false should keep blacklisted stock`() {
        EmergencyFilter.strictFiltering = false
        val analysis = sampleAnalysis()
        val out = EmergencyFilter.filterAnalysisResult(analysis.newsContent, analysis)

        // Non-strict -> keep all
        assertTrue(out.recommendedStocks.any { it.stockCode == "000001" })
        assertTrue(out.recommendedStocks.any { it.stockCode == "300750" })
    }
}
