package com.alphadoer.trader.data.utils

import android.util.Log
import com.alphadoer.trader.domain.model.AffectedSector
import com.alphadoer.trader.domain.model.AnalysisOptions
import com.alphadoer.trader.domain.model.NewsAnalysis
import com.alphadoer.trader.domain.model.RecommendedStock
import java.util.UUID

/**
 * 模拟数据生成器
 * 用于开发和测试（无API时）
 */
object MockDataGenerator {
    
    private const val TAG = "MockDataGenerator"
    
    // AI相关关键词
    private val aiKeywords = listOf("人工智能", "AI", "大模型", "机器学习", "深度学习", "神经网络", "ChatGPT", "GPT", "LLM")
    
    /**
     * 生成模拟的AI分析响应
     */
    fun generateMockAnalysis(
        newsContent: String,
        options: AnalysisOptions
    ): NewsAnalysis {
        val id = UUID.randomUUID().toString()
        
        // 检测是否为AI相关新闻
        val isAINews = aiKeywords.any { newsContent.contains(it, ignoreCase = true) }
        
        Log.d(TAG, "生成Mock分析，是否为AI新闻: $isAINews")
        
        val affectedSectors = if (isAINews) {
            generateTechSectors()
        } else {
            generateMockSectors()
        }
        
        val recommendedStocks = if (isAINews) {
            generateTechStocks(options.maxRecommendedStocks)
        } else {
            generateMockStocks(options.maxRecommendedStocks)
        }
        
        val summary = if (isAINews) {
            "这是一条关于人工智能的新闻分析。新闻内容涉及AI技术发展，预计将对科技板块产生显著影响。相关个股主要集中在计算机、电子、通信等科技板块。"
        } else {
            "这是一条${if (newsContent.length > 50) "重要" else "普通"}新闻的AI分析摘要。新闻内容涉及多个行业板块，预计将对市场产生${if (newsContent.length > 100) "显著" else "一定"}影响。"
        }
        
        return NewsAnalysis(
            id = id,
            newsContent = newsContent,
            summary = summary,
            sentiment = when (newsContent.length % 3) {
                0 -> NewsAnalysis.Sentiment.POSITIVE
                1 -> NewsAnalysis.Sentiment.NEGATIVE
                else -> NewsAnalysis.Sentiment.NEUTRAL
            },
            confidence = 0.75 + (newsContent.length % 25) / 100.0,
            keyPoints = listOf(
                "关键点1：新闻核心内容概述",
                "关键点2：市场影响预期",
                "关键点3：相关行业板块分析",
                "关键点4：投资建议和风险提示"
            ),
            affectedSectors = affectedSectors,
            recommendedStocks = recommendedStocks,
            riskWarnings = listOf(
                "市场波动风险：新闻可能引发短期市场波动",
                "政策风险：相关政策变化可能影响预期",
                "流动性风险：关注相关板块的流动性变化"
            ),
            recommendations = listOf(
                "建议关注相关板块的龙头个股",
                "注意控制仓位，避免过度集中",
                "密切关注后续政策动向和市场反应"
            ),
            analysisType = options.analysisType,
            createdAt = System.currentTimeMillis(),
            metadata = mapOf(
                "source" to "mock",
                "wordCount" to newsContent.length.toString(),
                "isAINews" to isAINews.toString()
            )
        )
    }
    
    private fun generateMockSectors(): List<AffectedSector> {
        return listOf(
            AffectedSector(
                sectorCode = "tech",
                sectorName = "科技板块",
                impactLevel = AffectedSector.ImpactLevel.HIGH,
                impactDescription = "新闻对科技板块影响较大，预计相关个股将出现波动",
                relatedStocks = listOf("000001", "000002")
            ),
            AffectedSector(
                sectorCode = "finance",
                sectorName = "金融板块",
                impactLevel = AffectedSector.ImpactLevel.MEDIUM,
                impactDescription = "对金融板块有一定影响，需关注政策变化",
                relatedStocks = listOf("600000", "600001")
            )
        )
    }
    
    private fun generateTechSectors(): List<AffectedSector> {
        return listOf(
            AffectedSector(
                sectorCode = "computer",
                sectorName = "计算机板块",
                impactLevel = AffectedSector.ImpactLevel.HIGH,
                impactDescription = "新闻对计算机板块影响较大，AI相关个股将受益",
                relatedStocks = listOf("002415", "000977", "600584")
            ),
            AffectedSector(
                sectorCode = "electronics",
                sectorName = "电子板块",
                impactLevel = AffectedSector.ImpactLevel.HIGH,
                impactDescription = "电子板块与AI技术密切相关，相关个股值得关注",
                relatedStocks = listOf("002230", "600703", "000063")
            ),
            AffectedSector(
                sectorCode = "communication",
                sectorName = "通信板块",
                impactLevel = AffectedSector.ImpactLevel.MEDIUM,
                impactDescription = "通信板块在AI发展中起到支撑作用",
                relatedStocks = listOf("600584", "002304")
            )
        )
    }
    
    private fun generateMockStocks(count: Int): List<RecommendedStock> {
        val stocks = listOf(
            RecommendedStock(
                stockCode = "000001",
                stockName = "平安银行",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.BUY,
                reason = "基本面良好，估值合理，符合当前市场趋势",
                confidence = 0.85,
                targetPrice = 15.50,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM
            ),
            RecommendedStock(
                stockCode = "600000",
                stockName = "浦发银行",
                market = "SH",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "值得关注，等待更好的买入时机",
                confidence = 0.70,
                riskLevel = RecommendedStock.RiskLevel.LOW
            ),
            RecommendedStock(
                stockCode = "000002",
                stockName = "万科A",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.HOLD,
                reason = "当前持有，建议继续观察",
                confidence = 0.65,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM
            ),
            RecommendedStock(
                stockCode = "600519",
                stockName = "贵州茅台",
                market = "SH",
                recommendation = RecommendedStock.RecommendationType.BUY,
                reason = "行业龙头，长期价值稳定",
                confidence = 0.90,
                targetPrice = 1800.00,
                riskLevel = RecommendedStock.RiskLevel.LOW
            ),
            RecommendedStock(
                stockCode = "000858",
                stockName = "五粮液",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "关注行业整体走势",
                confidence = 0.75,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM
            )
        )
        
        return stocks.take(count)
    }
    
    private fun generateTechStocks(count: Int): List<RecommendedStock> {
        val techStocks = listOf(
            RecommendedStock(
                stockCode = "002415",
                stockName = "海康威视",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.BUY,
                reason = "AI视觉技术领先，人工智能相关业务快速发展",
                confidence = 0.85,
                targetPrice = 45.00,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM
            ),
            RecommendedStock(
                stockCode = "000977",
                stockName = "浪潮信息",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.BUY,
                reason = "AI服务器龙头企业，大模型训练需求推动业绩增长",
                confidence = 0.80,
                targetPrice = 38.50,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM
            ),
            RecommendedStock(
                stockCode = "600584",
                stockName = "长电科技",
                market = "SH",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "芯片封装测试龙头，AI芯片需求增长带来机遇",
                confidence = 0.75,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM
            ),
            RecommendedStock(
                stockCode = "002230",
                stockName = "科大讯飞",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.BUY,
                reason = "AI语音技术领先，大模型研发投入持续增加",
                confidence = 0.82,
                targetPrice = 58.00,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM
            ),
            RecommendedStock(
                stockCode = "600703",
                stockName = "三安光电",
                market = "SH",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "LED芯片龙头，AI应用场景扩展带来新机遇",
                confidence = 0.70,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM
            ),
            RecommendedStock(
                stockCode = "000063",
                stockName = "中兴通讯",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "5G和AI技术结合，通信设备与AI应用协同发展",
                confidence = 0.75,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM
            )
        )
        
        return techStocks.take(count)
    }
}
