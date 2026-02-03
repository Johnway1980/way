package com.alphadoer.trader.data.util

import android.util.Log
import com.alphadoer.trader.data.remote.dto.AIAnalysisResponse
import com.alphadoer.trader.domain.model.AffectedSector
import com.alphadoer.trader.domain.model.NewsAnalysis
import com.alphadoer.trader.domain.model.RecommendedStock

/**
 * 紧急过滤工具
 * 用于过滤AI分析结果中的无关推荐
 */
object EmergencyFilter {
    
    private const val TAG = "EmergencyFilter"
    
    // 与AI新闻无关的板块
    private val unrelatedSectors = listOf("银行", "白酒", "房地产", "保险", "金融")
    
    // 与AI新闻无关的个股（即使AI推荐了也要过滤）
    private val unrelatedStocks = listOf(
        "000001", // 平安银行
        "600000", // 浦发银行
        "000002", // 万科A
        "600519", // 贵州茅台
        "000858"  // 五粮液
    )
    
    // AI相关关键词
    private val aiKeywords = listOf("人工智能", "AI", "大模型", "机器学习", "深度学习", "神经网络", "ChatGPT", "GPT", "LLM")
    
    // AI相关科技板块
    private val techSectors = listOf("计算机", "电子", "通信", "软件", "半导体", "芯片", "人工智能", "科技")
    
    /**
     * 过滤AI分析结果
     * 如果新闻包含AI关键词，强制过滤非科技股和无关板块
     */
    fun filterAnalysisResult(
        newsContent: String,
        analysis: NewsAnalysis
    ): NewsAnalysis {
        val containsAIKeywords = aiKeywords.any { newsContent.contains(it, ignoreCase = true) }
        
        if (!containsAIKeywords) {
            // 非AI新闻，不进行过滤
            return analysis
        }
        
        Log.d(TAG, "检测到AI相关新闻，开始过滤无关推荐")
        
        // 过滤板块
        val filteredSectors = analysis.affectedSectors.filter { sector ->
            val isRelated = techSectors.any { techSector ->
                sector.sectorName.contains(techSector, ignoreCase = true)
            }
            val isUnrelated = unrelatedSectors.any { unrelatedSector ->
                sector.sectorName.contains(unrelatedSector, ignoreCase = true)
            }
            
            if (isUnrelated) {
                Log.w(TAG, "过滤无关板块: ${sector.sectorName}")
                false
            } else {
                isRelated || !isUnrelated
            }
        }
        
        // 过滤股票
        val filteredStocks = analysis.recommendedStocks.filter { stock ->
            val isUnrelated = unrelatedStocks.contains(stock.stockCode)
            
            if (isUnrelated) {
                Log.w(TAG, "过滤无关股票: ${stock.stockCode} ${stock.stockName}")
                false
            } else {
                // 检查是否属于科技板块
                val isTechStock = isTechRelatedStock(stock)
                if (!isTechStock) {
                    Log.w(TAG, "过滤非科技股: ${stock.stockCode} ${stock.stockName}")
                }
                isTechStock
            }
        }
        
        val removedSectorsCount = analysis.affectedSectors.size - filteredSectors.size
        val removedStocksCount = analysis.recommendedStocks.size - filteredStocks.size

        if (removedSectorsCount > 0 || removedStocksCount > 0) {
            Log.w(TAG, "AI分析结果过滤统计: 板块=${removedSectorsCount}个, 股票=${removedStocksCount}个")
        }

        // 补偿逻辑：如果过滤后没有任何板块但仍有推荐股票，则尝试从股票的sectorName推导出板块
        val compensatedSectors = filteredSectors.toMutableList()

        if (compensatedSectors.isEmpty() && filteredStocks.isNotEmpty()) {
            Log.w(TAG, "过滤后没有板块，尝试从保留的股票中推导板块")

            // 以 sectorName 分组
            val groupedBySector = filteredStocks.groupBy { it.sectorName?.trim()?.ifEmpty { null } }

            groupedBySector.forEach { (sectorName, stocks) ->
                val derivedName = sectorName ?: run {
                    // 如果股票没有 sectorName，尝试从股票名称中匹配科技关键词
                    val nameMatch = listOf("科技", "电子", "软件", "通信", "芯片", "半导体", "人工智能").firstOrNull { kw ->
                        stocks.any { it.stockName.contains(kw, ignoreCase = true) }
                    }
                    nameMatch ?: "科技"
                }

                val sectorCode = derivedName.lowercase().replace(" ", "_")
                val relatedStockCodes = stocks.map { it.stockCode }

                compensatedSectors.add(
                    AffectedSector(
                        sectorCode = sectorCode,
                        sectorName = derivedName,
                        impactLevel = AffectedSector.ImpactLevel.MEDIUM,
                        impactDescription = "(derived from recommendedStocks)",
                        relatedStocks = relatedStockCodes
                    )
                )
            }

            Log.d(TAG, "已从 ${filteredStocks.size} 支股票推导出 ${compensatedSectors.size} 个板块")
        } else if (compensatedSectors.isNotEmpty() && filteredStocks.isNotEmpty()) {
            // 将保留的股票关联回现有板块（如果它们的 sectorName 与现有板块匹配）
            val sectorMap = compensatedSectors.associateBy { it.sectorName.lowercase() }.toMutableMap()
            filteredStocks.forEach { stock ->
                val sname = stock.sectorName?.trim()
                if (!sname.isNullOrBlank()) {
                    val key = sname.lowercase()
                    val existing = sectorMap[key]
                    if (existing != null && !existing.relatedStocks.contains(stock.stockCode)) {
                        val updated = existing.copy(relatedStocks = existing.relatedStocks + stock.stockCode)
                        val idx = compensatedSectors.indexOfFirst { it.sectorName.equals(existing.sectorName, ignoreCase = true) }
                        if (idx >= 0) compensatedSectors[idx] = updated
                    }
                }
            }
        }

        return analysis.copy(
            affectedSectors = compensatedSectors,
            recommendedStocks = filteredStocks
        )
    }
    
    /**
     * 判断股票是否属于科技相关
     */
    private fun isTechRelatedStock(stock: RecommendedStock): Boolean {
        // 通过股票代码判断（简化实现，实际应该查询股票信息）
        // 这里只做基础过滤，主要依赖黑名单
        val techStockCodes = listOf(
            "300750", "002415", "000977", "600584", "002230", // 示例科技股代码
            "688981", "300014", "002304", "000063", "600703"
        )
        
        // 如果股票名称包含科技关键词，认为相关
        val stockNameKeywords = listOf("科技", "电子", "软件", "通信", "芯片", "半导体", "人工智能")
        val nameContainsTech = stockNameKeywords.any { stock.stockName.contains(it) }
        
        return techStockCodes.contains(stock.stockCode) || nameContainsTech
    }
    
    /**
     * 构建AI提示词约束
     */
    fun buildPromptConstraints(): String {
        return """你是A股专业分析师，必须严格遵守以下规则：

板块推荐规则：
- 只推荐与新闻内容直接相关的板块
- 如果新闻提到"人工智能"，只能推荐"计算机"、"电子"等科技板块
- 严禁推荐无关板块（如金融、白酒、房地产等）

个股推荐规则：
- 股票必须属于相关板块
- 必须说明具体业务关联
- 禁止推荐与新闻无关的热门股

摘要要求：
- 必须包含新闻中的具体目标、时间、数字
- 禁止使用通用模板"""
    }
}
