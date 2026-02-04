package com.alphadoer.trader.data.util

import android.util.Log
import java.util.Locale
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
    // 航天/卫星相关关键词和板块
    private val spaceKeywords = listOf("星舰", "星链", "卫星", "发射", "航天", "太空", "星座", "IRIS2", "Starship", "Starlink")
    private val spaceSectors = listOf("航天科技", "卫星制造与发射", "卫星通信", "空间通信", "航天电子")
    
    /**
     * 过滤AI分析结果
     * 如果新闻包含AI关键词，强制过滤非科技股和无关板块
     */
    // 可在运行时关闭严格过滤以便调试/归档（非破坏性开关，默认启用）
    @Volatile
    var strictFiltering: Boolean = true

    fun filterAnalysisResult(
        newsContent: String,
        analysis: NewsAnalysis
    ): NewsAnalysis {
        val containsAIKeywords = aiKeywords.any { newsContent.contains(it, ignoreCase = true) }
        val containsSpaceKeywords = spaceKeywords.any { newsContent.contains(it, ignoreCase = true) }

        // 如果既不是AI新闻也不是航天/卫星新闻，则不进行过滤
        if (!containsAIKeywords && !containsSpaceKeywords) {
            return analysis
        }
        
        Log.d(TAG, "检测到AI相关新闻，开始过滤无关推荐")
        
        // 构建允许的板块集合：AI -> techSectors, 航天 -> spaceSectors
        val allowedSectors = mutableListOf<String>()
        if (containsAIKeywords) allowedSectors += techSectors
        if (containsSpaceKeywords) allowedSectors += spaceSectors

        // 过滤板块：保留与 allowedSectors 匹配且不在 unrelatedSectors 列表中的项
        val filteredSectors = if (!strictFiltering || allowedSectors.isEmpty()) {
            // 非严格模式或没有限定板块 -> 不进行板块级过滤
            analysis.affectedSectors
        } else {
            analysis.affectedSectors.filter { sector ->
                val isUnrelated = unrelatedSectors.any { unrelatedSector ->
                    sector.sectorName?.contains(unrelatedSector, ignoreCase = true) == true
                }
                if (isUnrelated) {
                    Log.w(TAG, "过滤无关板块: ${sector.sectorName}")
                    false
                } else {
                    allowedSectors.any { allowed -> sector.sectorName?.contains(allowed, ignoreCase = true) == true }
                }
            }
        }
        
        // 过滤股票
        val stockDiagnostics = mutableListOf<Pair<RecommendedStock, String>>()
        val filteredStocks = if (!strictFiltering) {
            // 非严格模式：保留所有推荐（仅用于诊断/回归验证）
            analysis.recommendedStocks
        } else {
            analysis.recommendedStocks.filter { stock ->
                val isUnrelated = unrelatedStocks.contains(stock.stockCode)

                if (isUnrelated) {
                    val reason = "blacklisted"
                    stockDiagnostics.add(stock to reason)
                    Log.w(TAG, "过滤无关股票: ${stock.stockCode} ${stock.stockName} -> $reason")
                    false
                } else {
                    // 检查是否属于相关板块（AI 或 航天）
                    val isTechStock = isTechRelatedStock(stock)
                    val isSpaceStock = (!stock.sectorName.isNullOrBlank() && spaceSectors.any { s -> stock.sectorName?.contains(s, ignoreCase = true) == true })
                        || spaceKeywords.any { kw -> stock.stockName.contains(kw, ignoreCase = true) }

                    val keepReason = when {
                        isTechStock -> "kept:tech"
                        isSpaceStock -> "kept:space"
                        else -> "filtered:not-tech-or-space"
                    }

                    if (keepReason.startsWith("kept")) {
                        stockDiagnostics.add(stock to keepReason)
                        Log.d(TAG, "保留股票: ${stock.stockCode} ${stock.stockName} -> $keepReason")
                    } else {
                        stockDiagnostics.add(stock to keepReason)
                        Log.w(TAG, "过滤非相关股: ${stock.stockCode} ${stock.stockName} -> $keepReason")
                    }

                    isTechStock || isSpaceStock
                }
            }
        }

        val removedSectorsCount = analysis.affectedSectors.size - filteredSectors.size
        val removedStocksCount = analysis.recommendedStocks.size - filteredStocks.size

        if (removedSectorsCount > 0 || removedStocksCount > 0) {
            Log.w(TAG, "AI分析结果过滤统计: 板块=${removedSectorsCount}个, 股票=${removedStocksCount}个")
        }

        // 输出每支股票的诊断理由，便于调试
        if (stockDiagnostics.isNotEmpty()) {
            Log.d(TAG, "股票过滤诊断：")
            stockDiagnostics.forEach { (stock, reason) ->
                Log.d(TAG, "diag:${stock.stockCode} | ${stock.stockName} | sector=${stock.sectorName} | reason=$reason")
            }

            // 同时把逐股诊断追加写入到项目目录下的诊断文件，方便CI/本地分析
            try {
                // 写入到单独文件，避免被测试覆盖（测试会写入 app/diagnostic-output.txt）
                val outPath = System.getProperty("user.dir") + java.io.File.separator + "app" + java.io.File.separator + "diagnostic-output-emergencyfilter.txt"
                val outFile = java.io.File(outPath)
                if (!outFile.exists()) outFile.createNewFile()

                // 写入头部统计（覆盖同名条目为追加，保留历史）
                val header = StringBuilder()
                header.append("RAW_LENGTH=${newsContent.length}\n")
                header.append("ORIG_STOCKS=${analysis.recommendedStocks.size}\n")
                // 过滤后股票数量和最终板块数会在写入时反映
                header.append("FILTERED_STOCKS=${filteredStocks.size}\n")
                // 补偿后的板块数量（记录当前已过滤保留的板块观察值）
                // 使用 filteredSectors.size 代替尚未计算的 compensatedSectors
                header.append("FINAL_SECTORS=${filteredSectors.size}\n\n")
                outFile.appendText(header.toString())

                stockDiagnostics.forEach { (stock, reason) ->
                    val line = "diag:${stock.stockCode} | ${stock.stockName} | sector=${stock.sectorName} | reason=$reason\n"
                    outFile.appendText(line)
                }
                outFile.appendText("\n")
            } catch (e: Exception) {
                Log.e(TAG, "写诊断文件失败: ${e.message}")
            }
        }

        // 补偿逻辑：如果过滤后没有任何板块但仍有推荐股票，则尝试从股票的sectorName推导出板块
        val compensatedSectors = filteredSectors.toMutableList()

        if (compensatedSectors.isEmpty() && filteredStocks.isNotEmpty()) {
            Log.w(TAG, "过滤后没有板块，尝试从保留的股票中推导板块")

            // 以 sectorName 分组
            val groupedBySector = filteredStocks.groupBy { it.sectorName?.trim()?.ifEmpty { null } }

            groupedBySector.forEach { (sectorName, stocks) ->
                val derivedName = sectorName ?: run {
                    // 如果股票没有 sectorName，尝试从股票名称中匹配航天/科技关键词
                    val nameMatch = listOf("航天", "卫星", "星链", "星舰", "发射").firstOrNull { kw ->
                        stocks.any { it.stockName.contains(kw, ignoreCase = true) }
                    }
                    if (nameMatch != null) nameMatch else if (containsSpaceKeywords) {
                        "航天科技"
                    } else {
                        listOf("科技", "电子", "通信", "半导体").firstOrNull { kw ->
                            stocks.any { it.stockName.contains(kw, ignoreCase = true) }
                        } ?: "科技"
                    }
                }

                val sectorCode = derivedName.lowercase(Locale.getDefault()).replace(" ", "_")
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
            val sectorMap = compensatedSectors.associateBy { it.sectorName.lowercase(Locale.getDefault()) }.toMutableMap()
            filteredStocks.forEach { stock ->
                val sname = stock.sectorName?.trim()
                if (!sname.isNullOrBlank()) {
                    val key = sname.lowercase(Locale.getDefault())
                    val existing = sectorMap[key]
                    if (existing != null && !existing.relatedStocks.contains(stock.stockCode)) {
                        val updated = existing.copy(relatedStocks = existing.relatedStocks + stock.stockCode)
                        val idx = compensatedSectors.indexOfFirst { it.sectorName.equals(existing.sectorName, ignoreCase = true) }
                        if (idx >= 0) compensatedSectors[idx] = updated
                    }
                }
            }

            // 确保每个板块至少有 3 支相关股票（尽量从 filteredStocks 中补齐）
            val minPerSector = 3
            // map stockCode -> RecommendedStock for quick lookup
            val stockByCode = filteredStocks.associateBy { it.stockCode }
            // 可用候选列表（去重）
            val availableCodes = filteredStocks.map { it.stockCode }.toMutableList()

            for (i in compensatedSectors.indices) {
                val sector = compensatedSectors[i]
                val current = sector.relatedStocks.toMutableList()

                // 1) 优先：同名 sector 的股票
                val sameSectorCodes = filteredStocks.filter {
                    !it.sectorName.isNullOrBlank() && it.sectorName.equals(sector.sectorName, ignoreCase = true)
                }.map { it.stockCode }
                for (code in sameSectorCodes) {
                    if (current.size >= minPerSector) break
                    if (!current.contains(code)) current.add(code)
                }

                // 2) 次优：名称中包含科技关键词的股票
                    // 2) 次优：优先使用航天相关候选（如果存在）
                    if (current.size < minPerSector) {
                        val spaceCandidates = filteredStocks.filter { rs ->
                            (!rs.sectorName.isNullOrBlank() && spaceSectors.any { s -> rs.sectorName?.contains(s, ignoreCase = true) == true })
                                    || spaceKeywords.any { kw -> rs.stockName.contains(kw, ignoreCase = true) }
                        }.map { it.stockCode }
                        for (code in spaceCandidates) {
                            if (current.size >= minPerSector) break
                            if (!current.contains(code)) current.add(code)
                        }
                    }

                    // 3) 次优2：名称中包含科技关键词的股票
                    if (current.size < minPerSector) {
                        val techCandidates = filteredStocks.filter { rs ->
                            val keywords = listOf("科技", "电子", "软件", "通信", "芯片", "半导体", "人工智能")
                            keywords.any { kw -> rs.stockName.contains(kw, ignoreCase = true) }
                        }.map { it.stockCode }
                        for (code in techCandidates) {
                            if (current.size >= minPerSector) break
                            if (!current.contains(code)) current.add(code)
                        }
                    }

                // 3) 兜底：从所有可用股票中补齐
                if (current.size < minPerSector) {
                    for (code in availableCodes) {
                        if (current.size >= minPerSector) break
                        if (!current.contains(code)) current.add(code)
                    }
                }

                // 截断或更新
                val finalRelated = if (current.size > minPerSector) current.take(minPerSector) else current
                compensatedSectors[i] = sector.copy(relatedStocks = finalRelated)
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
        val nameContainsTech = stockNameKeywords.any { stock.stockName.contains(it, ignoreCase = true) }
        
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
