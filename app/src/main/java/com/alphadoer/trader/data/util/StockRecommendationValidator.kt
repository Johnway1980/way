package com.alphadoer.trader.data.util

import android.util.Log
import com.alphadoer.trader.data.util.StockValidationTuning
import com.alphadoer.trader.domain.model.AffectedSector
import com.alphadoer.trader.domain.model.NewsAnalysis
import com.alphadoer.trader.domain.model.RecommendedStock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 股票推荐验证器
 * 对AI返回的分析结果进行数量补足和精度校准
 */
@Singleton
class StockRecommendationValidator @Inject constructor() {
    companion object {
        private const val TAG = "StockRecommendationValidator"
        private const val MIN_STOCKS_PER_SECTOR = 3 // 每个板块最少推荐股票数
        private const val MIN_CONFIDENCE_THRESHOLD = 0.5 // 最低置信度阈值
    }

    fun validateAndEnhance(
        analysis: NewsAnalysis,
        newsContent: String
    ): NewsAnalysis {
        // 这里只做结构修复，具体业务逻辑请补全
        return analysis
    }
    
    /**
     * 补足股票数量
     * 如果某个板块的股票数量不足3支，从预置股票池中补足
     * 包含多级降级机制：主要板块 -> 关联板块 -> 其他板块
     */
    private fun replenishStocks(
        analysis: NewsAnalysis,
        newsContent: String
    ): NewsAnalysis {
        val newsThemes = analysis.affectedSectors.map { it.sectorName }.filter { it.isNotBlank() }
        val newsKeywords = extractKeywords(newsContent)
        // 使用fold来累积结果，确保每个板块都能看到之前板块已使用的股票代码
        val (allReplenishedStocks, _) = analysis.affectedSectors.fold(
            Pair(emptyList<RecommendedStock>(), analysis.recommendedStocks.map { it.stockCode }.toMutableSet())
        ) { (accStocks, usedCodes), sector ->
            val sectorStocks = analysis.recommendedStocks.filter { 
                it.sectorName == sector.sectorName 
            }
            
            if (sectorStocks.size < MIN_STOCKS_PER_SECTOR) {
                val needed = MIN_STOCKS_PER_SECTOR - sectorStocks.size
                val existingCodesInSector = sectorStocks.map { it.stockCode }.toSet()
                
                Log.d(TAG, "板块 ${sector.sectorName} 当前有 ${sectorStocks.size} 支股票，需要补足 $needed 支")
                
                // 多级降级补足策略
                val replenishedStocks = mutableListOf<RecommendedStock>()
                var remainingNeeded = needed
                
                // 提取新闻核心领域（用于领域过滤）
                val newsDomain = extractNewsDomain(newsContent, newsKeywords)
                
                // 第一级：从本板块股票池选取（包含领域过滤）
                val primaryStocks = SectorStockPool.getStocksForSector(sector.sectorName)
                    .filter { it.stockCode !in usedCodes }
                    .filter { stock ->
                        // 领域相关性过滤
                        val companyRule = CompanyProfileRules.getRule(stock.stockCode)
                        val isDomainMatch = checkDomainMatch(companyRule?.businessDomain, newsDomain)
                        val isExcluded = isExcludedByTheme(companyRule, newsThemes)
                        val isCoreRelated = isCoreBusinessRelated(companyRule, newsThemes, newsKeywords)
                        
                        if (!isDomainMatch) {
                            Log.d(TAG, "补足时过滤股票 ${stock.stockName}(${stock.stockCode})：业务领域 ${companyRule?.businessDomain} 与新闻领域 $newsDomain 不匹配")
                        }
                        if (isExcluded && !isCoreRelated) {
                            Log.d(TAG, "补足时过滤股票 ${stock.stockName}(${stock.stockCode})：命中排除主题且核心业务不相关")
                        }
                        
                        isDomainMatch && (!isExcluded || isCoreRelated)
                    }
                    .take(remainingNeeded)
                
                replenishedStocks.addAll(primaryStocks.map { stock ->
                    stock.copy(
                        sectorName = sector.sectorName,
                        reason = generateReplenishmentReason(stock, sector, newsContent),
                        confidence = stock.confidence * 0.8 // 补足的股票置信度稍低
                    )
                })
                remainingNeeded -= primaryStocks.size
                
                Log.d(TAG, "板块 ${sector.sectorName} 从主要股票池补足 ${primaryStocks.size} 支，还需 $remainingNeeded 支")
                
                // 第二级：从上级/关联板块选取（如果主要板块股票不足）
                if (remainingNeeded > 0) {
                    val fallbackSectors = (getParentSectors(sector.sectorName) + getRelatedSectors(sector.sectorName))
                        .distinct()
                    var fallbackUsed = false
                    
                    for (relatedSector in fallbackSectors) {
                        if (remainingNeeded <= 0) break
                        
                        val relatedStocks = SectorStockPool.getStocksForSector(relatedSector)
                            .filter { it.stockCode !in usedCodes }
                            .filter { stock ->
                                // 领域相关性过滤
                                val companyRule = CompanyProfileRules.getRule(stock.stockCode)
                                val isDomainMatch = checkDomainMatch(companyRule?.businessDomain, newsDomain)
                                val isExcluded = isExcludedByTheme(companyRule, newsThemes)
                                val isCoreRelated = isCoreBusinessRelated(companyRule, newsThemes, newsKeywords)

                                if (isExcluded && !isCoreRelated) {
                                    Log.d(TAG, "补足时过滤股票 ${stock.stockName}(${stock.stockCode})：命中排除主题且核心业务不相关")
                                }

                                isDomainMatch && (!isExcluded || isCoreRelated)
                            }
                            .take(remainingNeeded)
                        
                        if (relatedStocks.isNotEmpty()) {
                            replenishedStocks.addAll(relatedStocks.map { stock ->
                                stock.copy(
                                    sectorName = sector.sectorName, // 标记为目标板块
                                    reason = generateFallbackReplenishmentReason(
                                        stock,
                                        sector,
                                        relatedSector,
                                        newsContent
                                    ),
                                    confidence = stock.confidence * 0.6 // 降级选取的股票置信度更低
                                )
                            })
                            remainingNeeded -= relatedStocks.size
                            fallbackUsed = true
                            Log.d(TAG, "板块 ${sector.sectorName} 从关联板块 $relatedSector 补足 ${relatedStocks.size} 支，还需 $remainingNeeded 支")
                        }
                    }
                    
                    if (!fallbackUsed && remainingNeeded > 0) {
                        Log.w(TAG, "板块 ${sector.sectorName} 无法从上级/关联板块获取股票")
                    }
                }
                
                // 第三级：从其他可用板块选取（如果关联板块也不足）
                if (remainingNeeded > 0) {
                    val allAvailableSectors = SectorStockPool.getSupportedSectors()
                        .filter { it != sector.sectorName && it !in getRelatedSectors(sector.sectorName) }
                    
                    for (otherSector in allAvailableSectors) {
                        if (remainingNeeded <= 0) break
                        
                        val otherStocks = SectorStockPool.getStocksForSector(otherSector)
                            .filter { it.stockCode !in usedCodes }
                            .filter { stock ->
                                // 领域相关性过滤
                                val companyRule = CompanyProfileRules.getRule(stock.stockCode)
                                val isDomainMatch = checkDomainMatch(companyRule?.businessDomain, newsDomain)
                                val isExcluded = isExcludedByTheme(companyRule, newsThemes)
                                val isCoreRelated = isCoreBusinessRelated(companyRule, newsThemes, newsKeywords)

                                if (isExcluded && !isCoreRelated) {
                                    Log.d(TAG, "补足时过滤股票 ${stock.stockName}(${stock.stockCode})：命中排除主题且核心业务不相关")
                                }

                                isDomainMatch && (!isExcluded || isCoreRelated)
                            }
                            .take(remainingNeeded)
                        
                        if (otherStocks.isNotEmpty()) {
                            replenishedStocks.addAll(otherStocks.map { stock ->
                                stock.copy(
                                    sectorName = sector.sectorName, // 标记为目标板块
                                    reason = generateFallbackReplenishmentReason(
                                        stock,
                                        sector,
                                        otherSector,
                                        newsContent
                                    ),
                                    confidence = stock.confidence * 0.5 // 最后降级选取的股票置信度最低
                                )
                            })
                            remainingNeeded -= otherStocks.size
                            Log.w(TAG, "板块 ${sector.sectorName} 从其他板块 $otherSector 降级补足 ${otherStocks.size} 支，还需 $remainingNeeded 支")
                        }
                    }
                }
                
                // 更新已使用股票代码集合（避免后续板块重复选取）
                usedCodes.addAll(replenishedStocks.map { it.stockCode })
                
                // 记录最终补足结果
                if (replenishedStocks.size >= needed) {
                    Log.d(TAG, "板块 ${sector.sectorName} 成功补足 ${replenishedStocks.size} 支股票（目标：$needed 支）")
                } else {
                    Log.w(
                        TAG,
                        "板块 ${sector.sectorName} 未能完全补足：当前 ${sectorStocks.size} 支，" +
                                "补足 ${replenishedStocks.size} 支，总计 ${sectorStocks.size + replenishedStocks.size} 支，" +
                                "仍缺 ${needed - replenishedStocks.size} 支。建议检查股票池配置。"
                    )
                }
                
                Pair(accStocks + replenishedStocks, usedCodes)
            } else {
                Log.d(TAG, "板块 ${sector.sectorName} 已有 ${sectorStocks.size} 支股票，无需补足")
                Pair(accStocks, usedCodes)
            }
        }
        
        val finalStocks = analysis.recommendedStocks + allReplenishedStocks
        
        // 验证最终结果：检查每个板块是否至少有3支股票
        val finalSectorCounts = finalStocks.groupBy { it.sectorName ?: "未知板块" }
        finalSectorCounts.forEach { (sectorName, stocks) ->
            if (stocks.size < MIN_STOCKS_PER_SECTOR) {
                Log.e(
                    TAG,
                    "警告：板块 $sectorName 最终只有 ${stocks.size} 支股票，" +
                            "少于最低要求 $MIN_STOCKS_PER_SECTOR 支。股票池配置可能不足。"
                )
            }
        }
        
        return analysis.copy(recommendedStocks = finalStocks)
    }
    
    /**
     * 获取关联板块列表
     * 定义板块之间的关联关系，用于降级选取
     */
    private fun getRelatedSectors(sectorName: String): List<String> {
        return sectorRelationships[sectorName] ?: emptyList()
    }
    
    /**
     * 板块关联关系映射
     * 定义哪些板块之间存在业务关联，用于降级选取股票
     */
    private val sectorRelationships: Map<String, List<String>> = mapOf(
        "人工智能" to listOf("半导体", "通信", "数据服务"), // AI板块与半导体、通信、数据服务关联
        "半导体" to listOf("人工智能", "通信"), // 半导体与AI、通信关联
        "稀土及新材料" to listOf("新能源"), // 稀土与新能材料关联
        "新能源" to listOf("稀土及新材料", "通信"), // 新能源与稀土、通信关联
        "通信" to listOf("人工智能", "半导体", "新能源", "工业互联网"), // 通信与多个板块关联
        "工业互联网" to listOf("数据服务", "通信", "人工智能"), // 工业互联网与数据服务、通信、AI关联
        "数据服务" to listOf("工业互联网", "人工智能"), // 数据服务与工业互联网、AI关联
        "卫星通信" to listOf("通信设备", "通信", "航天科技"),
        "通信设备" to listOf("卫星通信", "通信")
    )
    
    /**
     * 为降级选取的股票生成推荐理由
     */
    private fun generateFallbackReplenishmentReason(
        stock: RecommendedStock,
        targetSector: AffectedSector,
        sourceSector: String,
        newsContent: String
    ): String {
        val targetSectorName = targetSector.sectorName
        val impactDescription = targetSector.impactDescription
        
        return buildString {
            append("$targetSectorName 板块相关公司（来自关联板块 $sourceSector），")
            if (impactDescription.isNotBlank()) {
                append("受新闻影响：${impactDescription.take(50)}，")
            }
            append("值得关注")
        }
}
    
    /**
     * 过滤和排序股票
     * 根据公司特征规则库对股票进行相关性评分和排序
     * 包含领域相关性过滤和推荐理由质量检查
     */
    private fun filterAndSortStocks(
        stocks: List<RecommendedStock>,
        newsKeywords: List<String>,
        affectedSectors: List<AffectedSector>,
        newsContent: String = ""
    ): List<RecommendedStock> {
        // 提取新闻核心领域
        val newsDomain = extractNewsDomain(newsContent, newsKeywords)
        val newsThemes = affectedSectors.map { it.sectorName }.filter { it.isNotBlank() }
        
        // 第一步：业务领域降权（不直接过滤）
        val domainAdjustedStocks = stocks.map { stock ->
            val companyRule = CompanyProfileRules.getRule(stock.stockCode)
            val isDomainMatch = checkDomainMatch(companyRule?.businessDomain, newsDomain)
            if (!isDomainMatch) {
                Log.d(TAG, "降权股票 ${stock.stockName}(${stock.stockCode})：业务领域 ${companyRule?.businessDomain} 与新闻领域 $newsDomain 不匹配，置信度降至0.09")
                stock.copy(confidence = (stock.confidence?.coerceAtMost(0.09)) ?: 0.09)
            } else {
                stock
            }
        }

        // 第二步：跨主题强过滤（排除明确不相关主题）
        val themeFilteredStocks = domainAdjustedStocks.filter { stock ->
            val companyRule = CompanyProfileRules.getRule(stock.stockCode)
            val isExcluded = isExcludedByTheme(companyRule, newsThemes)
            val isCoreRelated = isCoreBusinessRelated(companyRule, newsThemes, newsKeywords)

            // 只保留未被强排除的股票
            !(isExcluded && !isCoreRelated)
        }
        // 返回过滤并排序后的股票列表（默认仅返回过滤结果，排序规则可根据需求扩展）
        return themeFilteredStocks
    }
    /**
     * 计算股票相关性评分
     * @param stock 股票
     * @param newsKeywords 新闻关键词
     * @param affectedSectors 受影响板块
     * @param newsDomain 新闻核心领域
     * @return 评分（0.0-1.0）
     */
    private fun calculateRelevanceScore(
        stock: RecommendedStock,
        newsKeywords: List<String>,
        affectedSectors: List<AffectedSector>,
        newsDomain: String? = null
    ): Double {
        var score = 0.5 // 基础分数
        
        // 获取公司规则
        val companyRule = CompanyProfileRules.getRule(stock.stockCode)
        if (companyRule != null) {
            // 领域匹配加分（如果领域匹配，给予额外加分）
            if (newsDomain != null && companyRule.businessDomain != null) {
                if (isDomainCompatible(companyRule.businessDomain, newsDomain)) {
                    score += 0.3 // 领域匹配给予高分
                }
            }
            
            // 检查正面触发器
            companyRule.positiveTriggers.forEach { trigger ->
                if (newsKeywords.any { it.contains(trigger, ignoreCase = true) }) {
                    score += 0.2
                }
            }
            
            // 检查负面触发器
            companyRule.negativeTriggers.forEach { trigger ->
                if (newsKeywords.any { it.contains(trigger, ignoreCase = true) }) {
                    score -= 0.5 // 负面触发器大幅降低分数
                }
            }
            
            // 检查标签匹配
            companyRule.tags.forEach { tag ->
                if (newsKeywords.any { it.contains(tag, ignoreCase = true) }) {
                    score += 0.1
                }
            }
        }
        
        // 检查板块匹配
        stock.sectorName?.let { sectorName ->
            if (affectedSectors.any { it.sectorName == sectorName }) {
                score += 0.1
            }
        }
        
        // 限制在0.0-1.0范围内
        return score.coerceIn(0.0, 1.0)
    }
    
    /**
     * 提取新闻核心领域
     * @param newsContent 新闻内容
     * @param keywords 新闻关键词
     * @return 新闻核心领域（如：工业、金融、科技等）
     */
    private fun extractNewsDomain(newsContent: String, keywords: List<String>): String? {
        // 领域关键词映射
        val domainKeywords = mapOf(
            "工业" to listOf("工业", "制造", "制造业", "工业数据", "工业互联网", "智能制造", "工业软件", "工业平台"),
            "金融" to listOf("金融", "银行", "证券", "保险", "投资", "理财", "金融数据"),
            "科技" to listOf("科技", "技术", "创新", "研发", "科技公司"),
            "数据" to listOf("数据", "数据服务", "数据标注", "数据治理", "数据挖掘")
        )
        
        // 检查新闻内容和关键词中的领域标识
        for ((domain, domainKeys) in domainKeywords) {
            if (domainKeys.any { key ->
                newsContent.contains(key, ignoreCase = true) || 
                keywords.any { it.contains(key, ignoreCase = true) }
            }) {
                Log.d(TAG, "识别新闻核心领域: $domain")
                return domain
            }
        }
        
        return null
    }
    
    /**
     * 检查领域匹配
     * @param companyDomain 公司业务领域
     * @param newsDomain 新闻核心领域
     * @return 是否匹配
     */
    private fun checkDomainMatch(companyDomain: String?, newsDomain: String?): Boolean {
        // 当启用严格模式时，任一为空视为不匹配；默认关闭以兼容旧数据
        if (companyDomain == null || newsDomain == null) {
            return !StockValidationTuning.strictNullDomainMismatch
        }
        
        // 领域兼容性检查
        return isDomainCompatible(companyDomain, newsDomain)
    }
    
    /**
     * 检查领域兼容性
     * @param companyDomain 公司业务领域
     * @param newsDomain 新闻核心领域
     * @return 是否兼容
     */
    private fun isDomainCompatible(companyDomain: String, newsDomain: String): Boolean {
        // 完全匹配
        if (companyDomain == newsDomain) {
            return true
        }
        
        // 领域兼容性映射
        val domainCompatibility = mapOf(
            "科技" to setOf("工业", "数据"), // 科技领域与工业、数据兼容
            "工业" to setOf("科技", "数据"), // 工业领域与科技、数据兼容
            "数据" to setOf("科技", "工业"), // 数据领域与科技、工业兼容
            "金融" to setOf() // 金融领域只与金融兼容
        )
        
        // 检查兼容性
        val compatibleDomains = domainCompatibility[companyDomain] ?: emptySet()
        return newsDomain in compatibleDomains || companyDomain == newsDomain
    }

    /**
     * 检查公司是否被某些新闻主题明确排除
     */
    private fun isExcludedByTheme(companyRule: CompanyProfileRules.CompanyRule?, newsThemes: List<String>): Boolean {
        if (companyRule == null) return false
        if (companyRule.excludedNewsThemes.isEmpty()) return false

        return companyRule.excludedNewsThemes.any { excluded ->
            newsThemes.any { theme ->
                theme.equals(excluded, ignoreCase = true) || theme.contains(excluded, ignoreCase = true) || excluded.contains(theme, ignoreCase = true)
            }
        }
    }

    /**
     * 检查公司核心业务是否与新闻主题/关键词相关
     */
    private fun isCoreBusinessRelated(
        companyRule: CompanyProfileRules.CompanyRule?,
        newsThemes: List<String>,
        newsKeywords: List<String>
    ): Boolean {
        if (companyRule == null) return false

        // 优先检查明确的核心业务标签
        val coreTags = (companyRule.coreBusinessTags ?: emptyList()) + listOfNotNull(companyRule.coreBusiness)

        if (coreTags.any { tag -> newsThemes.any { it.equals(tag, ignoreCase = true) || it.contains(tag, ignoreCase = true) } }) {
            return true
        }

        // 检查新闻关键词是否命中公司核心业务标签
        if (coreTags.any { tag -> newsKeywords.any { kw -> kw.contains(tag, ignoreCase = true) } }) {
            return true
        }

        // 作为兜底，检查公司标签集合与新闻关键词的交集
        if (companyRule.tags.any { t -> newsKeywords.any { kw -> kw.contains(t, ignoreCase = true) } }) {
            return true
        }

        return false
    }
    
    /**
     * 检查推荐理由质量
     * 过滤空洞的推荐理由
     * @param reason 推荐理由
     * @return 是否通过质量检查
     */
    private fun checkReasonQuality(reason: String): Boolean {
        if (reason.isBlank()) {
            return false
        }
        
        // 空洞理由模式（需要过滤）
        val emptyReasonPatterns = listOf(
            "板块相关公司",
            "相关公司",
            "值得关注",
            "值得关注。",
            "板块相关",
            "相关股票"
        )
        
        // 检查是否只包含空洞理由
        val trimmedReason = reason.trim()
        val isOnlyEmptyReason = emptyReasonPatterns.any { pattern ->
            trimmedReason == pattern || 
            trimmedReason == "$pattern。" ||
            trimmedReason.startsWith(pattern) && trimmedReason.length <= pattern.length + 5
        }
        
        // 如果只包含空洞理由，过滤掉
        if (isOnlyEmptyReason) {
            return false
        }
        
        // 检查理由长度（太短的可能是空洞理由）
        if (trimmedReason.length < StockValidationTuning.minReasonLength) {
            return false
        }
        
        return true
    }
    
    /**
     * 业务领域强过滤
     * 在补足股票数量之前，移除与新闻核心业务领域完全无关的股票
     * @param analysis AI返回的分析结果
     * @param newsContent 新闻内容
     * @return 过滤后的分析结果
     */
    private fun applyBusinessDomainStrongFilter(
        analysis: NewsAnalysis,
        newsContent: String
    ): NewsAnalysis {
        // 提取新闻的核心业务领域
        val newsPrimaryDomain = extractNewsPrimaryBusinessDomain(newsContent, analysis.affectedSectors)
        
        // 如果无法识别新闻的核心业务领域，跳过强过滤（向后兼容）
        if (newsPrimaryDomain == null) {
            Log.d(TAG, "无法识别新闻核心业务领域，跳过业务领域强过滤")
            return analysis
        }
        
        Log.d(TAG, "识别新闻核心业务领域: $newsPrimaryDomain，开始业务领域强过滤")
        
        // 过滤股票：移除与新闻核心业务领域完全无关的股票
        val filteredStocks = analysis.recommendedStocks.filter { stock ->
            val companyRule = CompanyProfileRules.getRule(stock.stockCode)
            val companyPrimaryDomain = companyRule?.primaryBusinessDomain
            
            // 如果公司没有定义核心业务领域，保留（向后兼容）
            if (companyPrimaryDomain == null) {
                Log.d(TAG, "股票 ${stock.stockName}(${stock.stockCode}) 未定义核心业务领域，保留")
                return@filter true
            }
            
            // 检查是否与新闻核心业务领域完全无关
            val isUnrelated = isBusinessDomainUnrelated(companyPrimaryDomain, newsPrimaryDomain)
            
            if (isUnrelated) {
                Log.d(TAG, "强过滤移除股票 ${stock.stockName}(${stock.stockCode})：" +
                        "公司核心业务领域 '$companyPrimaryDomain' 与新闻核心业务领域 '$newsPrimaryDomain' 完全无关")
                return@filter false
            }
            
            true
        }
        
        val removedCount = analysis.recommendedStocks.size - filteredStocks.size
        if (removedCount > 0) {
            Log.d(TAG, "业务领域强过滤完成：移除 $removedCount 支无关股票，" +
                    "剩余 ${filteredStocks.size} 支股票")
        } else {
            Log.d(TAG, "业务领域强过滤完成：未发现无关股票")
        }
        
        return analysis.copy(recommendedStocks = filteredStocks)
    }
    
    /**
     * 提取新闻的核心业务领域
     * 优先从已识别的精准板块中提取，其次从新闻内容中提取
     * @param newsContent 新闻内容
     * @param affectedSectors 已识别的受影响板块
     * @return 新闻核心业务领域（如：工业制造、金融、数据服务、云计算等）
     */
    private fun extractNewsPrimaryBusinessDomain(
        newsContent: String,
        affectedSectors: List<AffectedSector>
    ): String? {
        // 优先从已识别的精准板块中提取核心业务领域
        val sectorToDomainMapping = mapOf(
            "工业互联网" to "工业制造",
            "数据服务" to "数据服务",
            "人工智能" to "数据服务", // AI通常涉及数据服务
            "半导体" to "半导体制造",
            "通信" to "通信服务",
            "新能源" to "新能源制造",
            "稀土及新材料" to "新材料制造"
        )
        
        // 检查已识别的板块，优先使用精准板块
        val preciseSectors = listOf("工业互联网", "数据服务", "人工智能", "半导体", "通信", "新能源", "稀土及新材料")
        for (sector in affectedSectors) {
            if (sector.sectorName in preciseSectors) {
                val domain = sectorToDomainMapping[sector.sectorName]
                if (domain != null) {
                    Log.d(TAG, "从精准板块 '${sector.sectorName}' 提取核心业务领域: $domain")
                    return domain
                }
            }
        }
        
        // 如果精准板块未匹配，从新闻内容中提取
        val contentKeywords = extractKeywords(newsContent)
        val newsContentLower = newsContent.lowercase()
        
        // 定义关键词到核心业务领域的映射
        val keywordToDomainMapping = mapOf(
            // 工业制造相关
            listOf("工业数据", "工业互联网", "智能制造", "工业软件", "工业平台", "制造业数字化转型") to "工业制造",
            // 数据服务相关
            listOf("数据标注", "数据服务", "数据治理", "数据挖掘", "数据采集", "数据处理") to "数据服务",
            // 金融相关
            listOf("金融", "银行", "证券", "保险", "投资", "理财", "金融IT", "金融软件") to "金融",
            // 云计算相关
            listOf("云计算", "云服务", "云平台", "数据中心", "服务器") to "云计算",
            // 半导体制造相关
            listOf("芯片", "半导体", "晶圆", "集成电路") to "半导体制造",
            // 通信服务相关
            listOf("5G", "通信", "网络建设", "基站") to "通信服务",
            // 新能源制造相关
            listOf("新能源", "新能源汽车", "电池", "锂电池", "电动车") to "新能源制造"
        )
        
        // 按优先级检查关键词
        for ((keywords, domain) in keywordToDomainMapping) {
            if (keywords.any { keyword ->
                newsContentLower.contains(keyword.lowercase()) ||
                contentKeywords.any { it.contains(keyword, ignoreCase = true) }
            }) {
                Log.d(TAG, "从新闻关键词提取核心业务领域: $domain")
                return domain
            }
        }
        
        return null
    }
    
    /**
     * 检查两个业务领域是否完全无关
     * @param companyDomain 公司的核心业务领域
     * @param newsDomain 新闻的核心业务领域
     * @return 是否完全无关（true表示无关，应过滤）
     */
    private fun isBusinessDomainUnrelated(
        companyDomain: String,
        newsDomain: String
    ): Boolean {
        // 完全匹配，相关
        if (companyDomain == newsDomain) {
            return false
        }
        
        // 定义无关领域组合（如果公司领域和新闻领域在同一个无关组合中，则视为无关）
        val unrelatedDomainGroups = listOf(
            // 金融领域与其他领域完全无关
            setOf("金融", "工业制造"),
            setOf("金融", "数据服务"),
            setOf("金融", "云计算"),
            setOf("金融", "半导体制造"),
            setOf("金融", "通信服务"),
            setOf("金融", "新能源制造"),
            setOf("金融", "新材料制造"),
            
            // 工业制造与金融完全无关（已包含在上面的组合中）
            // 但工业制造可以与数据服务、云计算相关（工业互联网、工业数据等）
            
            // 数据服务与金融完全无关（已包含在上面的组合中）
            // 但数据服务可以与工业制造、云计算相关
        )
        
        // 检查是否在无关组合中
        for (unrelatedGroup in unrelatedDomainGroups) {
            if (companyDomain in unrelatedGroup && newsDomain in unrelatedGroup) {
                Log.d(TAG, "业务领域 '$companyDomain' 与 '$newsDomain' 在无关组合中: $unrelatedGroup")
                return true
            }
        }
        
        // 定义相关领域组合（如果公司领域和新闻领域在同一个相关组合中，则视为相关）
        val relatedDomainGroups = listOf(
            // 工业制造与数据服务相关（工业数据、工业互联网等）
            setOf("工业制造", "数据服务"),
            // 工业制造与云计算相关（工业云平台等）
            setOf("工业制造", "云计算"),
            // 数据服务与云计算相关（数据服务通常需要云计算支持）
            setOf("数据服务", "云计算"),
            // 数据服务与工业制造相关（已包含在上面）
            // 半导体制造与通信服务相关（5G芯片等）
            setOf("半导体制造", "通信服务")
        )
        
        // 检查是否在相关组合中
        for (relatedGroup in relatedDomainGroups) {
            if (companyDomain in relatedGroup && newsDomain in relatedGroup) {
                Log.d(TAG, "业务领域 '$companyDomain' 与 '$newsDomain' 在相关组合中: $relatedGroup")
                return false
            }
        }
        
        // 默认情况下，如果不在无关组合中，也不在相关组合中，则视为可能相关（不过滤）
        // 这样可以避免过度过滤
        return false
    }
    
    /**
     * 提取新闻关键词
     */
    private fun extractKeywords(newsContent: String): List<String> {
        // 简单的关键词提取（可以根据需要增强）
        val keywords = mutableListOf<String>()
        
        // 常见的关键词模式（按优先级排序，精确关键词在前）
        val patterns = listOf(
            // 工业互联网相关（高优先级）
            "工业数据", "工业互联网", "智能制造", "制造业数字化转型", "工业物联网",
            "工业大数据", "工业软件", "工业平台", "工业云",
            // 数据服务相关（高优先级）
            "数据标注", "数据咨询", "数据集", "数据服务", "数据治理",
            "数据挖掘", "数据采集", "数据处理",
            // 其他关键词
            "人工智能", "AI", "大模型", "芯片", "半导体", "新能源", "光伏", 
            "电池", "新能源汽车", "5G", "通信", "云计算", "数据中心",
            "稀土", "新材料", "化工", "医药", "生物制药", "医疗器械",
            "金融", "银行", "保险", "证券", "房地产", "白酒", "消费",
            "进口", "出口", "贸易", "关税", "政策", "监管",
            "计算机", "电子" // 宽泛板块，优先级较低
        )
        
        patterns.forEach { pattern ->
            if (newsContent.contains(pattern, ignoreCase = true)) {
                keywords.add(pattern)
            }
        }
        
        return keywords
    }
    
    /**
     * 根据新闻关键词修正或补充板块
     * 确保精确的关键词映射到正确的板块，并过滤掉宽泛的上级板块
     * @param analysis AI返回的分析结果
     * @param newsContent 新闻内容
     * @return 修正后的分析结果
     */
    private fun correctSectorsByKeywords(
        analysis: NewsAnalysis,
        newsContent: String
    ): NewsAnalysis {
        val keywords = extractKeywords(newsContent)
        val existingSectorNames = analysis.affectedSectors.map { it.sectorName }.toSet()
        val correctedSectors = analysis.affectedSectors.toMutableList()
        val addedSectors = mutableSetOf<String>()
        val sectorsToRemove = mutableSetOf<String>() // 需要移除的宽泛板块
        
        // 检查关键词到板块的映射
        keywordToSectorMapping.forEach { (keywordPattern, targetSectors) ->
            // 检查新闻中是否包含该关键词
            if (keywords.any { it.contains(keywordPattern, ignoreCase = true) } ||
                newsContent.contains(keywordPattern, ignoreCase = true)) {
                
                // 为每个目标板块检查是否已存在
                targetSectors.forEach { targetSector ->
                    if (targetSector !in existingSectorNames && targetSector !in addedSectors) {
                        // 添加缺失的精确板块
                        correctedSectors.add(
                            AffectedSector(
                                sectorCode = targetSector.lowercase().replace(" ", "_"),
                                sectorName = targetSector,
                                impactLevel = AffectedSector.ImpactLevel.HIGH, // 精确匹配设为高影响
                                impactDescription = "根据关键词 '$keywordPattern' 识别为 $targetSector 板块",
                                relatedStocks = emptyList()
                            )
                        )
                        addedSectors.add(targetSector)
                        Log.d(TAG, "根据关键词 '$keywordPattern' 添加精确板块: $targetSector")
                        
                        // 检查是否需要移除对应的宽泛上级板块
                        getParentSectors(targetSector).forEach { parentSector ->
                            if (parentSector in existingSectorNames) {
                                sectorsToRemove.add(parentSector)
                                Log.d(TAG, "移除宽泛上级板块: $parentSector（因已添加精确板块: $targetSector）")
                            }
                        }
                    }
                }
            }
        }
        
        // 移除宽泛的上级板块
        if (sectorsToRemove.isNotEmpty()) {
            correctedSectors.removeAll { it.sectorName in sectorsToRemove }
            Log.d(TAG, "已移除宽泛板块: ${sectorsToRemove.joinToString(", ")}")
        }
        
        // 如果添加了新板块或移除了宽泛板块，返回修正后的分析结果
        if (addedSectors.isNotEmpty() || sectorsToRemove.isNotEmpty()) {
            Log.d(TAG, "板块修正完成，新增板块: ${addedSectors.joinToString(", ")}, 移除板块: ${sectorsToRemove.joinToString(", ")}")
            return analysis.copy(affectedSectors = correctedSectors)
        }
        
        return analysis
    }
    
    /**
     * 获取板块的宽泛上级板块列表
     * 定义板块层级关系：精确板块 -> 宽泛板块
     */
    private fun getParentSectors(sectorName: String): List<String> {
        return sectorHierarchy[sectorName] ?: emptyList()
    }
    
    /**
     * 板块层级关系映射
     * 定义精确板块与其宽泛上级板块的关系
     * 格式：精确板块（子级）-> 宽泛上级板块列表（父级）
     * 
     * 注意：这个映射用于识别哪些板块是更精准的子级板块，哪些是宽泛的父级板块
     * 当同时存在父子板块时，系统会保留子级板块，移除父级板块，并将父级板块的股票合并到子级板块
     */
    private val sectorHierarchy: Map<String, List<String>> = mapOf(
        // 工业互联网板块的上级板块
        "工业互联网" to listOf("计算机", "电子", "软件", "计算机/电子"),
        // 数据服务板块的上级板块
        "数据服务" to listOf("计算机", "电子", "软件", "信息服务", "计算机/电子"),
        // 人工智能板块的上级板块
        "人工智能" to listOf("计算机", "电子", "软件", "计算机/电子"),
        // 半导体板块的上级板块
        "半导体" to listOf("电子", "计算机", "计算机/电子"),
        // 通信板块的上级板块
        "通信" to listOf("电子", "计算机", "通信设备", "计算机/电子"),
        // 卫星通信板块的上级板块
        "卫星通信" to listOf("通信设备", "通信", "电子")
    )
    
    /**
     * 移除父级板块并合并股票到子级板块
     * 实现"精准板块优先"的去重机制：如果两个板块存在父子层级关系，仅保留子级（更精准的）板块
     * 被删除的父级板块下的推荐个股，会合并到保留的子级板块列表中，并去重
     * 
     * @param sectors 原始板块列表
     * @param stocks 原始股票列表
     * @return Pair<去重后的板块列表, 合并后的股票列表>
     */
    private fun removeParentSectorsAndMergeStocks(
        sectors: List<AffectedSector>,
        stocks: List<RecommendedStock>
    ): Pair<List<AffectedSector>, List<RecommendedStock>> {
        if (sectors.isEmpty()) {
            return Pair(sectors, stocks)
        }
        
        val sectorNames = sectors.map { it.sectorName }.toSet()
        val sectorsToRemove = mutableSetOf<String>() // 需要移除的父级板块
        val sectorMapping = mutableMapOf<String, String>() // 父级板块 -> 子级板块的映射（用于合并股票）
        
        // 遍历所有板块，检查是否存在父子层级关系
        sectors.forEach { sector ->
            val sectorName = sector.sectorName
            // 获取该板块的所有父级板块
            val parentSectors = getParentSectors(sectorName)
            
            // 检查父级板块是否也在列表中
            parentSectors.forEach { parentSector ->
                if (parentSector in sectorNames) {
                    // 发现父子关系：保留子级板块，移除父级板块
                    sectorsToRemove.add(parentSector)
                    sectorMapping[parentSector] = sectorName
                    Log.d(TAG, "发现板块层级关系：$parentSector（父级）-> $sectorName（子级），将移除父级板块并合并股票")
                }
            }
        }
        
        // 如果没有需要移除的板块，直接返回
        if (sectorsToRemove.isEmpty()) {
            return Pair(sectors, stocks)
        }
        
        // 移除父级板块
        val filteredSectors = sectors.filter { it.sectorName !in sectorsToRemove }
        
        // 合并股票：将父级板块的股票合并到对应的子级板块
        val mergedStocks = stocks.map { stock ->
            val stockSectorName = stock.sectorName
            if (stockSectorName != null && stockSectorName in sectorsToRemove) {
                // 该股票属于被移除的父级板块，需要合并到子级板块
                val targetSector = sectorMapping[stockSectorName]
                if (targetSector != null) {
                    Log.d(TAG, "合并股票 ${stock.stockName}(${stock.stockCode})：从父级板块 $stockSectorName 合并到子级板块 $targetSector")
                    stock.copy(sectorName = targetSector)
                } else {
                    stock // 如果找不到目标子级板块，保持原样（理论上不应该发生）
                }
            } else {
                stock // 不属于被移除的板块，保持原样
            }
        }.distinctBy { "${it.stockCode}_${it.sectorName}" } // 去重：同一股票代码+板块名称的组合只保留一个
        
        Log.d(TAG, "板块层级去重完成：移除 ${sectorsToRemove.size} 个父级板块（${sectorsToRemove.joinToString(", ")}），" +
                "板块数量 ${sectors.size} -> ${filteredSectors.size}，股票数量 ${stocks.size} -> ${mergedStocks.size}")
        
        return Pair(filteredSectors, mergedStocks)
    }
    
    /**
     * 板块去重和合并
     * 确保板块名称唯一，合并同一板块的不同影响描述
     * @param sectors 原始板块列表
     * @return 去重后的板块列表
     */
    private fun deduplicateAndMergeSectors(sectors: List<AffectedSector>): List<AffectedSector> {
        if (sectors.isEmpty()) return sectors
        
        // 按板块名称分组
        val groupedSectors = sectors.groupBy { it.sectorName }
        
        // 对每个板块进行合并
        val mergedSectors = groupedSectors.map { (sectorName, sectorList) ->
            if (sectorList.size == 1) {
                // 只有一个，直接返回
                sectorList.first()
            } else {
                // 多个相同板块，合并影响描述和影响级别
                val highestImpact = sectorList.maxOfOrNull { 
                    when (it.impactLevel) {
                        AffectedSector.ImpactLevel.HIGH -> 3
                        AffectedSector.ImpactLevel.MEDIUM -> 2
                        AffectedSector.ImpactLevel.LOW -> 1
                    }
                } ?: 2
                
                val mergedImpactLevel = when (highestImpact) {
                    3 -> AffectedSector.ImpactLevel.HIGH
                    1 -> AffectedSector.ImpactLevel.LOW
                    else -> AffectedSector.ImpactLevel.MEDIUM
                }
                
                // 合并影响描述（去重）
                val mergedDescriptions = sectorList
                    .mapNotNull { it.impactDescription }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .joinToString("；")
                
                // 合并相关股票代码（去重）
                val mergedRelatedStocks = sectorList
                    .flatMap { it.relatedStocks }
                    .distinct()
                
                AffectedSector(
                    sectorCode = sectorList.first().sectorCode,
                    sectorName = sectorName,
                    impactLevel = mergedImpactLevel,
                    impactDescription = mergedDescriptions.ifBlank { sectorList.first().impactDescription },
                    relatedStocks = mergedRelatedStocks
                )
            }
        }
        
        if (mergedSectors.size < sectors.size) {
            Log.d(TAG, "板块去重完成：${sectors.size} -> ${mergedSectors.size}，合并了 ${sectors.size - mergedSectors.size} 个重复板块")
        }
        
        return mergedSectors
    }

    /**
     * 根据最终精准板块列表，重写个股的所属板块标签
     * 确保每条个股的 sectorName 与 affectedSectors 中的精准板块名称保持一致
     * 同时同步更新推荐理由中出现的旧宽泛板块名称
     *
     * @param stocks 当前股票列表（可能仍包含宽泛板块标签，如“计算机”）
     * @param finalSectors 最终确定的精准板块列表
     */
    private fun rewriteStockSectorsWithFinalSectors(
        stocks: List<RecommendedStock>,
        finalSectors: List<AffectedSector>
    ): List<RecommendedStock> {
        if (stocks.isEmpty() || finalSectors.isEmpty()) return stocks

        val finalSectorNames = finalSectors.map { it.sectorName }.toSet()

        // 构建“父级板块 -> 精准子级板块集合”的反向映射，便于从宽泛板块跳转到精准板块
        val parentToChildSectors: Map<String, List<String>> = sectorHierarchy
            .flatMap { (child, parents) ->
                parents.map { parent -> parent to child }
            }
            .groupBy({ it.first }, { it.second })

        return stocks.map { stock ->
            val originalSector = stock.sectorName

            // 没有板块标签，或已经是最终板块之一，则无需重写
            if (originalSector.isNullOrBlank() || originalSector in finalSectorNames) {
                return@map stock
            }

            // 根据层级关系，从父级板块映射到精准子级板块（且该子级板块在最终板块列表中）
            val candidateChildren = parentToChildSectors[originalSector]
                ?.filter { it in finalSectorNames }
                .orEmpty()

            if (candidateChildren.isEmpty()) {
                // 找不到对应的精准板块，保留原始标签，但记录日志便于后续扩展规则
                Log.d(
                    TAG,
                    "股票 ${stock.stockName}(${stock.stockCode}) 的板块 '$originalSector' 未找到对应的精准子级板块，保持不变"
                )
                return@map stock
            }

            // 多个候选时，优先选择在最终板块列表中影响级别更高的板块；若并列，取列表顺序第一个
            val targetSectorName = candidateChildren
                .map { childName ->
                    val sector = finalSectors.firstOrNull { it.sectorName == childName }
                    val impactScore = when (sector?.impactLevel) {
                        AffectedSector.ImpactLevel.HIGH -> 3
                        AffectedSector.ImpactLevel.MEDIUM -> 2
                        AffectedSector.ImpactLevel.LOW -> 1
                        else -> 0
                    }
                    childName to impactScore
                }
                .maxByOrNull { it.second }
                ?.first
                ?: candidateChildren.first()

            // 同步更新推荐理由中出现的旧板块名称（包括“xxx板块”形式）
            val oldReason = stock.reason
            val updatedReason = oldReason
                .replace("${originalSector}板块", "$targetSectorName 板块")
                .replace(originalSector, targetSectorName)

            Log.d(
                TAG,
                "重写股票 ${stock.stockName}(${stock.stockCode}) 板块标签: " +
                        "'$originalSector' -> '$targetSectorName'"
            )

            stock.copy(
                sectorName = targetSectorName,
                reason = updatedReason
            )
        }
    }

    /**
     * 按股票代码进行去重
     * 如果同一股票代码出现多次，优先保留：
     * 1. 置信度更高的条目
     * 2. 若置信度相同，则保留在原始列表中出现更早的条目
     */
    private fun deduplicateStocksByCode(
        stocks: List<RecommendedStock>
    ): List<RecommendedStock> {
        if (stocks.isEmpty()) return stocks

        // 先记住原始顺序索引，便于在置信度相同的情况下选择更早出现的条目
        val indexedStocks = stocks.mapIndexed { index, stock -> index to stock }

        val bestByCode = indexedStocks
            .groupBy { (_, stock) -> stock.stockCode }
            .mapValues { (_, entries) ->
                entries.maxWithOrNull { a, b ->
                    val (idxA, stockA) = a
                    val (idxB, stockB) = b

                    val confA = stockA.confidence
                    val confB = stockB.confidence

                    when {
                        confA != null && confB != null && confA != confB ->
                            confA.compareTo(confB) // 置信度高的优先

                        confA != null && confB == null ->
                            1 // 有置信度比没置信度优先

                        confA == null && confB != null ->
                            -1

                        else ->
                            // 置信度相同或都为空：保留原始顺序更靠前的
                            -idxA.compareTo(idxB)
                    }
                }!!
            }

        // 保持整体顺序基本不乱：按原始索引排序后返回股票对象
        return bestByCode.values
            .sortedBy { (index, _) -> index }
            .map { (_, stock) -> stock }
    }

    /**
     * 对“澄清公告 / 传言证伪”类新闻应用特殊策略：
     * - 情绪统一调整为 NEUTRAL（中性偏谨慎）
     * - 个股推荐严格收缩到与公告方高度相关的公司（新闻中直接提到的公司）
     * - 推荐理由和风险提示中加入“澄清公告 / 传言风险”的提示，建议以观望为主
     */
    private fun applyClarificationPolicy(
        analysis: NewsAnalysis,
        newsContent: String
    ): NewsAnalysis {
        if (!isClarificationNews(newsContent)) {
            return analysis
        }

        Log.d(TAG, "检测到澄清/传言证伪类新闻，应用澄清公告策略")

        val contentLower = newsContent.lowercase()

        // 1. 识别与公告直接相关的公司：名称或代码在新闻正文中出现
        val directlyRelatedStocks = analysis.recommendedStocks.filter { stock ->
            val name = stock.stockName
            val code = stock.stockCode
            val nameMatched = name.isNotBlank() && contentLower.contains(name.lowercase())
            val codeMatched = code.isNotBlank() && contentLower.contains(code.lowercase())
            nameMatched || codeMatched
        }

        val finalStocks = when {
            directlyRelatedStocks.isNotEmpty() -> {
                Log.d(
                    TAG,
                    "澄清公告策略：仅保留与新闻正文直接关联的股票 ${
                        directlyRelatedStocks.joinToString { it.stockName }
                    }"
                )
                directlyRelatedStocks
            }

            analysis.recommendedStocks.isNotEmpty() -> {
                // 如果无法识别直接相关公司，为避免列表完全为空，最多保留1-2只作为“事件相关重点关注”
                val fallback = analysis.recommendedStocks.take(2)
                Log.w(
                    TAG,
                    "澄清公告策略：未能识别直接相关公司，保留前 ${fallback.size} 只股票作为事件相关重点关注"
                )
                fallback
            }

            else -> emptyList()
        }

        // 2. 调整这些股票的推荐级别和理由：统一为 WATCH + 观望/谨慎
        val adjustedStocks = finalStocks.map { stock ->
            val baseReason = stock.reason.ifBlank {
                "与本次澄清公告直接相关的公司，短期情绪扰动较大。"
            }
            val reasonWithCaution = buildString {
                append(baseReason.trim())
                if (!baseReason.contains("观望") && !baseReason.contains("谨慎")) {
                    append(" 建议以观望为主，注意澄清公告后的情绪波动风险。")
                }
            }

            stock.copy(
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = reasonWithCaution
            )
        }

        // 3. 调整情绪、关键点、风险提示和建议
        val clarifiedKeyPoints = buildList {
            add("本条新闻属于澄清/传言证伪类公告，相关传言已被公司或监管机构明确澄清。")
            addAll(analysis.keyPoints)
        }

        val clarifiedRiskWarnings = buildList {
            add("本事件为澄清公告，短期股价可能存在情绪波动，投资者需警惕基于传言的交易风险，建议以观望为主。")
            addAll(analysis.riskWarnings)
        }

        val clarifiedRecommendations = buildList {
            add("鉴于本次为澄清/传言证伪类公告，当前阶段以观望为主，避免基于未经证实信息的追涨杀跌。")
            addAll(analysis.recommendations)
        }

        return analysis.copy(
            sentiment = NewsAnalysis.Sentiment.NEUTRAL,
            keyPoints = clarifiedKeyPoints,
            recommendedStocks = adjustedStocks,
            riskWarnings = clarifiedRiskWarnings,
            recommendations = clarifiedRecommendations
        )
    }

    /**
     * 判断新闻是否为“澄清公告 / 传言证伪”类型
     */
    private fun isClarificationNews(newsContent: String): Boolean {
        val lower = newsContent.lowercase()

        // 关键词集合：澄清、不属实、否认、辟谣、传言、谣言等
        val coreKeywords = listOf(
            "澄清公告",
            "澄清说明",
            "澄清声明",
            "发布澄清",
            "澄清",
            "不属实",
            "并不属实",
            "失实",
            "不实消息",
            "不实信息",
            "谣言",
            "传言",
            "并无此事",
            "并不存在",
            "予以否认",
            "否认",
            "辟谣"
        )

        val matched = coreKeywords.any { lower.contains(it.lowercase()) }
        if (matched) {
            Log.d(TAG, "根据关键词识别为澄清/传言证伪类新闻：命中关键词")
        }
        return matched
    }
    
    /**
     * 关键词到板块的精确映射规则
     * 格式：关键词模式 -> 目标板块列表（按优先级排序）
     * 注意：精确的关键词映射应该优先于宽泛的板块
     */
    private val keywordToSectorMapping: Map<String, List<String>> = mapOf(
        // 卫星通信相关关键词 -> 卫星通信板块（最高优先级）
        "星链" to listOf("卫星通信"),
        "星链V3" to listOf("卫星通信"),
        "星链V2" to listOf("卫星通信"),
        "卫星通信" to listOf("卫星通信"),
        "卫星互联网" to listOf("卫星通信"),
        "低轨" to listOf("卫星通信"),
        "低轨星座" to listOf("卫星通信"),
        "低轨卫星" to listOf("卫星通信"),
        "卫星测控" to listOf("卫星通信"),
        "卫星测绘" to listOf("卫星通信"),
        "IRIS2" to listOf("卫星通信"),
        "Starlink" to listOf("卫星通信"),

        // 航天科技相关关键词
        "航天" to listOf("航天科技"),
        "火箭" to listOf("航天科技"),
        "运载" to listOf("航天科技"),

        // 工业互联网相关关键词 -> 工业互联网板块（最高优先级）
        "工业数据" to listOf("工业互联网", "数据服务"),
        "工业互联网" to listOf("工业互联网"),
        "智能制造" to listOf("工业互联网"),
        "制造业数字化转型" to listOf("工业互联网"),
        "工业物联网" to listOf("工业互联网"),
        "工业大数据" to listOf("工业互联网", "数据服务"),
        "工业软件" to listOf("工业互联网"),
        "工业平台" to listOf("工业互联网"),
        "工业云" to listOf("工业互联网"),
        
        // 数据服务相关关键词 -> 数据服务板块（最高优先级）
        "数据标注" to listOf("数据服务"),
        "数据咨询" to listOf("数据服务"),
        "数据集" to listOf("数据服务"),
        "数据服务" to listOf("数据服务"),
        "数据治理" to listOf("数据服务"),
        "数据挖掘" to listOf("数据服务"),
        "数据采集" to listOf("数据服务"),
        "数据处理" to listOf("数据服务")
    )
    
    /**
     * 为补足的股票生成推荐理由
     */
    private fun generateReplenishmentReason(
        stock: RecommendedStock,
        sector: AffectedSector,
        newsContent: String
    ): String {
        val sectorName = sector.sectorName
        val impactDescription = sector.impactDescription
        
        return buildString {
            append("$sectorName 板块相关公司，")
            if (impactDescription.isNotBlank()) {
                append("受新闻影响：${impactDescription.take(50)}，")
            }
            append("值得关注")
        }
    }

}
