package com.alphadoer.trader.data.repository

import com.alphadoer.trader.data.local.dao.AIAnalysisCacheDao
import com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity
import com.alphadoer.trader.data.remote.api.AIService
import com.alphadoer.trader.data.remote.dto.QianfanChatRequest
import com.alphadoer.trader.data.remote.dto.QianfanChatResponse
import com.alphadoer.trader.data.remote.dto.ChatMessage
import com.alphadoer.trader.data.remote.dto.ContentItem
import com.alphadoer.trader.data.remote.interceptor.HttpException
import com.alphadoer.trader.data.remote.interceptor.NetworkException
import android.util.Log
import com.alphadoer.trader.data.util.DiagnosticsLogger
import com.alphadoer.trader.data.util.EmergencyFilter
import com.alphadoer.trader.data.util.StockRecommendationValidator
import com.alphadoer.trader.data.utils.MockDataGenerator
import com.alphadoer.trader.domain.model.AffectedSector
import com.alphadoer.trader.domain.model.AnalysisOptions
import com.alphadoer.trader.domain.model.NewsAnalysis
import com.alphadoer.trader.domain.model.RecommendedStock
import com.alphadoer.trader.domain.repository.NewsAnalysisRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

/**
 * 新闻分析Repository实现
 */
class NewsAnalysisRepositoryImpl @Inject constructor(
    private val aiService: AIService,
    private val aiAnalysisCacheDao: AIAnalysisCacheDao,
    private val stockRecommendationValidator: StockRecommendationValidator
) : NewsAnalysisRepository {
    
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    
    override suspend fun analyzeNews(
        newsContent: String,
        options: AnalysisOptions
    ): Result<NewsAnalysis> {
        return try {
            // 配置：是否使用Mock数据
            // 注意：如果API URL是占位符（如api.example.com），API调用将失败并fallback到Mock数据
            val useMockData = false // 默认尝试使用真实API
            
            // 记录诊断信息
            DiagnosticsLogger.logAnalysisStart(newsContent, useMockData)
            
            val analysis = if (useMockData) {
                // 使用Mock数据
                DiagnosticsLogger.logMockDataGeneration(newsContent)
                MockDataGenerator.generateMockAnalysis(newsContent, options)
            } else {
                // 构建百度千帆API请求
                val systemPrompt = buildString {
                    appendLine("你是A股专业分析师，请分析以下新闻内容，并返回JSON格式的分析结果。")
                    appendLine(EmergencyFilter.buildPromptConstraints())
                    appendLine()
                    appendLine("股票推荐要求（必须严格遵守）：")
                    appendLine("- 每个受影响的板块必须至少推荐3支相关股票")
                    appendLine("- 如果新闻涉及多个板块，每个板块都要推荐至少3支股票")
                    appendLine("- 总共推荐股票数量 = 板块数量 × 3（例如：2个板块至少推荐6支股票）")
                    appendLine("- 优先推荐板块内的龙头股、代表性股票和成长性股票")
                    appendLine("- 每支股票必须说明与新闻内容的具体业务关联")
                    appendLine("- 严禁因为板块数量少而减少股票推荐数量")
                    appendLine("- 新闻涉及多个板块时，每支推荐股票必须标明所属板块（使用sectorName字段）")
                    appendLine("- 每支股票的sectorName必须与affectedSectors中的板块名称对应")
                    appendLine()
                    appendLine("请严格按照以下JSON格式返回，必须使用标准JSON格式（所有字符串必须用英文双引号，所有逗号必须正确）：")
                    appendLine("{")
                    appendLine("  \"summary\": \"分析摘要\",")
                    appendLine("  \"sentiment\": \"POSITIVE|NEGATIVE|NEUTRAL\",")
                    appendLine("  \"confidence\": 0.7,")
                    appendLine("  \"keyPoints\": [\"关键点1\", \"关键点2\"],")
                    appendLine("  \"recommendations\": [\"建议1\", \"建议2\"],")
                    appendLine("  \"affectedSectors\": [{\"sectorName\": \"板块名\", \"impactLevel\": \"HIGH|MEDIUM|LOW\", \"impactDescription\": \"影响描述\"}],")
                    appendLine("  \"recommendedStocks\": [{\"stockCode\": \"股票代码\", \"stockName\": \"股票名称\", \"market\": \"SH|SZ|BJ\", \"sectorName\": \"所属板块名称\", \"reason\": \"推荐理由\", \"recommendation\": \"BUY|WATCH|HOLD\", \"confidence\": 0.8}],")
                    appendLine("  \"riskWarnings\": [\"风险提示1\", \"风险提示2\"]")
                    appendLine("}")
                    appendLine()
                    appendLine("重要提示：")
                    appendLine("- 必须返回完整的、有效的JSON格式")
                    appendLine("- 所有字符串值必须用英文双引号包裹")
                    appendLine("- 数组元素之间必须用逗号分隔")
                    appendLine("- 对象属性之间必须用逗号分隔")
                    appendLine("- 最后一个属性后不能有逗号")
                    appendLine("- 确保所有大括号和方括号正确匹配")
                }
                
                val userPrompt = buildString {
                    appendLine("请分析以下新闻：")
                    appendLine()
                    appendLine(newsContent)
                    appendLine()
                    if (options.focusSectors != null) {
                        appendLine("重点关注板块：${options.focusSectors.joinToString(", ")}")
                    }
                    if (options.excludeStocks != null) {
                        appendLine("排除股票：${options.excludeStocks.joinToString(", ")}")
                    }
                    if (newsContent.contains("人工智能", ignoreCase = true) || 
                        newsContent.contains("AI", ignoreCase = true) ||
                        newsContent.contains("大模型", ignoreCase = true)) {
                        appendLine("注意：严禁推荐金融、白酒、房地产、保险等无关板块和股票。")
                    }
                }
                
                // 根据官方示例，content 必须是数组格式：[{"type": "text", "text": "..."}]
                val request = QianfanChatRequest(
                    model = "ernie-4.5-turbo-vl-latest", // 使用官方示例中的模型
                    messages = listOf(
                        ChatMessage(
                            role = "system", 
                            content = listOf(ContentItem(type = "text", text = systemPrompt))
                        ),
                        ChatMessage(
                            role = "user", 
                            content = listOf(ContentItem(type = "text", text = userPrompt))
                        )
                    ),
                    stream = false, // 根据官方示例设置为 false
                    temperature = 0.7,
                    topP = 0.8,
                    penaltyScore = 1.0
                )
                
                Log.d("NewsAnalysisRepository", "发送AI分析请求，内容长度: ${newsContent.length}")
                DiagnosticsLogger.logRequest(request)
                
                // 调用API
                val response = try {
                    aiService.analyzeNews(request)
                } catch (e: Exception) {
                    DiagnosticsLogger.logNetworkError(e, newsContent)
                    // 网络错误时fallback到Mock数据，避免闪退
                    Log.e("NewsAnalysisRepository", "API调用失败，使用Mock数据: ${e.message}", e)
                    val mockAnalysis = MockDataGenerator.generateMockAnalysis(newsContent, options)
                    val filteredMockAnalysis = if (newsContent.contains("人工智能", ignoreCase = true) || 
                        newsContent.contains("AI", ignoreCase = true) ||
                        newsContent.contains("大模型", ignoreCase = true)) {
                        EmergencyFilter.filterAnalysisResult(newsContent, mockAnalysis)
                    } else {
                        mockAnalysis
                    }
                    DiagnosticsLogger.logAnalysisResult(filteredMockAnalysis)
                    saveAnalysis(filteredMockAnalysis)
                    return Result.success(filteredMockAnalysis)
                }
                
                // 记录API响应
                DiagnosticsLogger.logApiResponse(response)
                Log.d("NewsAnalysisRepository", "API响应: errorCode=${response.errorCode}, errorMsg=${response.errorMsg}")
                
                // 检查API错误
                if (response.errorCode != null || response.errorMsg != null) {
                    val errorMessage = response.errorMsg ?: "API返回错误"
                    Log.e("NewsAnalysisRepository", "API返回错误: $errorMessage (code: ${response.errorCode})，使用Mock数据作为fallback")
                    val mockAnalysis = MockDataGenerator.generateMockAnalysis(newsContent, options)
                    val filteredMockAnalysis = if (newsContent.contains("人工智能", ignoreCase = true) || 
                        newsContent.contains("AI", ignoreCase = true) ||
                        newsContent.contains("大模型", ignoreCase = true)) {
                        EmergencyFilter.filterAnalysisResult(newsContent, mockAnalysis)
                    } else {
                        mockAnalysis
                    }
                    DiagnosticsLogger.logAnalysisResult(filteredMockAnalysis)
                    saveAnalysis(filteredMockAnalysis)
                    return Result.success(filteredMockAnalysis)
                }
                
                // 从响应中提取内容
                // 优先从 choices[0].message.content 获取，如果没有则从 result 获取
                val responseContent = response.choices?.firstOrNull()?.message?.content 
                    ?: response.result
                
                Log.d("NewsAnalysisRepository", "提取的响应内容长度: ${responseContent?.length ?: 0}")
                Log.d("NewsAnalysisRepository", "响应内容摘要: ${responseContent?.take(200)}")
                
                // 解析AI返回的JSON结果
                val analysisResult = try {
                    if (responseContent.isNullOrBlank()) {
                        throw Exception("API返回结果为空")
                    }
                    parseQianfanResponse(responseContent, newsContent, options)
                } catch (e: Exception) {
                    Log.e("NewsAnalysisRepository", "解析API响应失败: ${e.message}，使用Mock数据作为fallback", e)
                    val mockAnalysis = MockDataGenerator.generateMockAnalysis(newsContent, options)
                    val filteredMockAnalysis = if (newsContent.contains("人工智能", ignoreCase = true) || 
                        newsContent.contains("AI", ignoreCase = true) ||
                        newsContent.contains("大模型", ignoreCase = true)) {
                        EmergencyFilter.filterAnalysisResult(newsContent, mockAnalysis)
                    } else {
                        mockAnalysis
                    }
                    DiagnosticsLogger.logAnalysisResult(filteredMockAnalysis)
                    saveAnalysis(filteredMockAnalysis)
                    return Result.success(filteredMockAnalysis)
                }
                
                // 紧急过滤：如果新闻包含AI关键词，强制过滤非科技股
                val filteredResult = if (newsContent.contains("人工智能", ignoreCase = true) || 
                    newsContent.contains("AI", ignoreCase = true) ||
                    newsContent.contains("大模型", ignoreCase = true)) {
                    Log.d("NewsAnalysisRepository", "检测到AI相关新闻，应用紧急过滤")
                    EmergencyFilter.filterAnalysisResult(newsContent, analysisResult)
                } else {
                    analysisResult
                }
                
                // 验证和增强：数量补足和精度校准
                val enhancedResult = try {
                    Log.d("NewsAnalysisRepository", "开始验证和增强分析结果")
                    stockRecommendationValidator.validateAndEnhance(filteredResult, newsContent)
                } catch (e: Exception) {
                    Log.e("NewsAnalysisRepository", "验证和增强失败: ${e.message}，使用原始结果", e)
                    filteredResult // 验证失败时使用原始结果
                }
                
                DiagnosticsLogger.logAnalysisResult(enhancedResult)
                enhancedResult
            }
            
            // 紧急过滤：对Mock数据也进行过滤（如果新闻包含AI关键词）
            val filteredAnalysis = if (newsContent.contains("人工智能", ignoreCase = true) || 
                newsContent.contains("AI", ignoreCase = true) ||
                newsContent.contains("大模型", ignoreCase = true)) {
                Log.d("NewsAnalysisRepository", "检测到AI相关新闻（Mock数据），应用紧急过滤")
                EmergencyFilter.filterAnalysisResult(newsContent, analysis)
            } else {
                analysis
            }
            
            // 验证和增强：数量补足和精度校准
            val enhancedAnalysis = try {
                Log.d("NewsAnalysisRepository", "开始验证和增强分析结果（Mock数据）")
                stockRecommendationValidator.validateAndEnhance(filteredAnalysis, newsContent)
            } catch (e: Exception) {
                Log.e("NewsAnalysisRepository", "验证和增强失败: ${e.message}，使用原始结果", e)
                filteredAnalysis // 验证失败时使用原始结果
            }
            
            // 记录最终结果
            DiagnosticsLogger.logAnalysisResult(enhancedAnalysis)
            
            // 保存到缓存
            saveAnalysis(enhancedAnalysis)
            
            Result.success(enhancedAnalysis)
        } catch (e: NetworkException) {
            // 网络错误时使用Mock数据作为fallback
            Log.w("NewsAnalysisRepository", "网络错误，使用Mock数据作为fallback")
            DiagnosticsLogger.logNetworkError(e, newsContent)
            DiagnosticsLogger.logMockDataGeneration(newsContent)
            val mockAnalysis = MockDataGenerator.generateMockAnalysis(newsContent, options)
            // 对Mock数据也进行过滤
            val filteredMockAnalysis = if (newsContent.contains("人工智能", ignoreCase = true) || 
                newsContent.contains("AI", ignoreCase = true) ||
                newsContent.contains("大模型", ignoreCase = true)) {
                EmergencyFilter.filterAnalysisResult(newsContent, mockAnalysis)
            } else {
                mockAnalysis
            }
            // 验证和增强
            val enhancedMockAnalysis = try {
                stockRecommendationValidator.validateAndEnhance(filteredMockAnalysis, newsContent)
            } catch (e2: Exception) {
                Log.e("NewsAnalysisRepository", "验证和增强失败: ${e2.message}，使用原始结果", e2)
                filteredMockAnalysis
            }
            DiagnosticsLogger.logAnalysisResult(enhancedMockAnalysis)
            saveAnalysis(enhancedMockAnalysis)
            Result.success(enhancedMockAnalysis)
        } catch (e: HttpException) {
            // HTTP错误时使用Mock数据作为fallback，避免闪退
            Log.w("NewsAnalysisRepository", "HTTP错误，使用Mock数据作为fallback: ${e.message}")
            DiagnosticsLogger.logNetworkError(e, newsContent)
            DiagnosticsLogger.logMockDataGeneration(newsContent)
            val mockAnalysis = MockDataGenerator.generateMockAnalysis(newsContent, options)
            val filteredMockAnalysis = if (newsContent.contains("人工智能", ignoreCase = true) || 
                newsContent.contains("AI", ignoreCase = true) ||
                newsContent.contains("大模型", ignoreCase = true)) {
                EmergencyFilter.filterAnalysisResult(newsContent, mockAnalysis)
            } else {
                mockAnalysis
            }
            // 验证和增强
            val enhancedMockAnalysis = try {
                stockRecommendationValidator.validateAndEnhance(filteredMockAnalysis, newsContent)
            } catch (e2: Exception) {
                Log.e("NewsAnalysisRepository", "验证和增强失败: ${e2.message}，使用原始结果", e2)
                filteredMockAnalysis
            }
            DiagnosticsLogger.logAnalysisResult(enhancedMockAnalysis)
            saveAnalysis(enhancedMockAnalysis)
            Result.success(enhancedMockAnalysis)
        } catch (e: Exception) {
            // 任何其他异常都使用Mock数据作为fallback，避免闪退
            Log.e("NewsAnalysisRepository", "发生异常，使用Mock数据作为fallback: ${e.message}", e)
            DiagnosticsLogger.logNetworkError(e, newsContent)
            DiagnosticsLogger.logMockDataGeneration(newsContent)
            val mockAnalysis = MockDataGenerator.generateMockAnalysis(newsContent, options)
            val filteredMockAnalysis = if (newsContent.contains("人工智能", ignoreCase = true) || 
                newsContent.contains("AI", ignoreCase = true) ||
                newsContent.contains("大模型", ignoreCase = true)) {
                EmergencyFilter.filterAnalysisResult(newsContent, mockAnalysis)
            } else {
                mockAnalysis
            }
            // 验证和增强
            val enhancedMockAnalysis = try {
                stockRecommendationValidator.validateAndEnhance(filteredMockAnalysis, newsContent)
            } catch (e2: Exception) {
                Log.e("NewsAnalysisRepository", "验证和增强失败: ${e2.message}，使用原始结果", e2)
                filteredMockAnalysis
            }
            DiagnosticsLogger.logAnalysisResult(enhancedMockAnalysis)
            saveAnalysis(enhancedMockAnalysis)
            Result.success(enhancedMockAnalysis)
        }
    }
    
    // Testing helper: expose parsing for unit tests (package-visible)
    internal fun parseQianfanResponseForTest(
        jsonString: String,
        newsContent: String,
        options: com.alphadoer.trader.domain.model.AnalysisOptions
    ): com.alphadoer.trader.domain.model.NewsAnalysis {
        return parseQianfanResponse(jsonString, newsContent, options)
    }
    
    override fun getAnalysisHistory(): Flow<List<NewsAnalysis>> = flow {
        try {
            val cached = try {
                aiAnalysisCacheDao.getCachesByType("NEWS")
            } catch (e: Exception) {
                Log.e("NewsAnalysisRepository", "数据库查询失败: ${e.message}", e)
                emptyList()
            }
            
            val analyses = cached.mapNotNull { entity ->
                try {
                    entity.toDomainModel()
                } catch (e: Exception) {
                    Log.e("NewsAnalysisRepository", "转换分析结果失败: ${e.message}", e)
                    null
                }
            }
            emit(analyses)
        } catch (e: Exception) {
            Log.e("NewsAnalysisRepository", "获取分析历史失败: ${e.message}", e)
            emit(emptyList()) // 发生错误时返回空列表，避免崩溃
        }
    }
    
    override suspend fun getAnalysisById(id: String): NewsAnalysis? {
        return try {
            val cached = try {
                aiAnalysisCacheDao.getCacheByKey(id)
            } catch (e: Exception) {
                Log.e("NewsAnalysisRepository", "数据库查询失败: ${e.message}", e)
                null
            }
            cached?.toDomainModel()
        } catch (e: Exception) {
            Log.e("NewsAnalysisRepository", "获取分析结果失败: ${e.message}", e)
            null
        }
    }
    
    override suspend fun saveAnalysis(analysis: NewsAnalysis): Result<Unit> {
        return try {
            val entity = analysis.toEntity()
            aiAnalysisCacheDao.insertCache(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteAnalysis(id: String): Result<Unit> {
        return try {
            aiAnalysisCacheDao.deleteCacheByKey(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCachedAnalysis(newsContent: String): NewsAnalysis? {
        // 使用内容hash作为缓存键
        val cacheKey = "news_${newsContent.hashCode()}"
        val cached = aiAnalysisCacheDao.getCacheByKey(cacheKey)
        
        // 检查是否过期
        if (cached != null && cached.expiresAt != null) {
            if (System.currentTimeMillis() > cached.expiresAt) {
                return null // 已过期
            }
        }
        
        return cached?.toDomainModel()
    }
    
    // ========== 数据转换 ==========
    /**
     * 修复JSON格式问题
     * 处理AI返回的不完整或格式错误的JSON
     */
    private fun fixJsonFormat(json: String): String {
        var fixed = json
        
        // 如果JSON不完整（缺少结束大括号），尝试修复
        if (!fixed.trim().endsWith("}")) {
            // 计算大括号是否匹配
            val openBraces = fixed.count { it == '{' }
            val closeBraces = fixed.count { it == '}' }
            val missingBraces = openBraces - closeBraces
            
            if (missingBraces > 0) {
                // 在末尾添加缺失的结束大括号
                fixed = fixed.trimEnd() + "}".repeat(missingBraces)
                Log.d("NewsAnalysisRepository", "修复JSON：添加了 $missingBraces 个结束大括号")
            }
        }
        
        // 修复常见的引号问题（中文引号替换为英文引号）
        fixed = fixed.replace(""", "\"")
            .replace(""", "\"")
            .replace("'", "'")
            .replace("'", "'")
        
        // 修复缺少逗号的问题（在 } 和 " 之间，或 ] 和 " 之间）
        fixed = fixed.replace(Regex("""\}\s*"([^"]+)":"""), """}, "$1":""")
        fixed = fixed.replace(Regex("""\]\s*"([^"]+)":"""), """], "$1":""")
        
        // 修复数组元素之间缺少逗号的问题
        fixed = fixed.replace(Regex("""\]\s*\["""), """], [""")
        
        // 修复字符串值缺少引号的问题（在冒号后）
        fixed = fixed.replace(Regex("""":\s*([^",\[\]{}]+)([,}\]])""")) { matchResult ->
            val value = matchResult.groupValues[1].trim()
            val suffix = matchResult.groupValues[2]
            // 如果值看起来像字符串且不包含引号，添加引号
            if (value.isNotEmpty() && !value.startsWith("\"") && !value.matches(Regex("""^-?\d+\.?\d*$"""))) {
                """: "$value"$suffix"""
            } else {
                matchResult.value
            }
        }
        
        return fixed
    }
    
    /**
     * 解析百度千帆API返回的JSON字符串
     * 使用简单的字符串操作来解析，避免依赖org.json
     */
    private fun parseQianfanResponse(
        jsonString: String,
        newsContent: String,
        options: AnalysisOptions
    ): NewsAnalysis {
        val id = UUID.randomUUID().toString()

        Log.d("NewsAnalysisRepository", "原始AI响应长度=${jsonString.length}")
        Log.d("NewsAnalysisRepository", "原始AI响应前500=${jsonString.take(500)}")

        fun tryParseToMap(input: String): Map<String, Any?>? {
            return try {
                val adapter = moshi.adapter(Map::class.java)
                @Suppress("UNCHECKED_CAST")
                adapter.fromJson(input) as? Map<String, Any?>
            } catch (e: Exception) {
                null
            }
        }

        fun extractJsonFragment(raw: String, key: String): String? {
            val pattern = Regex("\"${Regex.escape(key)}\"\\s*:\\s*(\\[.*?\\]|\\{.*?\\})", RegexOption.DOT_MATCHES_ALL)
            val m = pattern.find(raw)
            return m?.groups?.get(1)?.value
        }

        fun unwrapPossibleStringWrappedJson(s: String): String {
            var r = s.trim()
            r = r.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            if (r.startsWith("\"") && r.endsWith("\"")) {
                r = r.substring(1, r.length - 1)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\t", "\t")
                    .replace("\\\\", "\\")
                    .trim()
            }
            return r
        }

        try {
            var cleaned = unwrapPossibleStringWrappedJson(jsonString)
            cleaned = cleaned.trim()
            cleaned = cleaned.removePrefix("\uFEFF")
            try {
                cleaned = fixJsonFormat(cleaned)
            } catch (_: Exception) { }

            Log.d("NewsAnalysisRepository", "尝试整体解析，长度=${cleaned.length}")

            var json: Map<String, Any?>? = tryParseToMap(cleaned)

            if (json == null) {
                Log.w("NewsAnalysisRepository", "整体JSON解析失败，尝试从响应中提取嵌套JSON")
                val keysToTry = listOf("result", "data", "choices", "content", "message", "output", "analysis", "analysis_result")
                for (k in keysToTry) {
                    val frag = extractJsonFragment(jsonString, k)
                    if (!frag.isNullOrBlank()) {
                        val candidate = unwrapPossibleStringWrappedJson(frag)
                        val payload = if (candidate.trimStart().startsWith("[")) {
                            "{\"wrapper\": $candidate}"
                        } else candidate
                        json = tryParseToMap(payload)
                        if (json != null) {
                            if (payload.startsWith("{\"wrapper\":")) {
                                val wrapper = json["wrapper"]
                                json = if (wrapper is List<*>) mapOf(k to wrapper) else mapOf(k to wrapper)
                            }
                            Log.d("NewsAnalysisRepository", "从字段 '$k' 成功提取并解析 JSON")
                            break
                        }
                    }
                }
            }

            if (json == null) {
                val objMatch = Regex("\\{(?:[^{}]|(?R))*\\}", RegexOption.DOT_MATCHES_ALL).find(cleaned)
                if (objMatch != null) {
                    val candidate = unwrapPossibleStringWrappedJson(objMatch.value)
                    json = tryParseToMap(candidate)
                    if (json != null) {
                        Log.d("NewsAnalysisRepository", "从原始响应中匹配到对象并解析成功")
                    }
                }
            }

            if (json == null) {
                val excerpt = jsonString.take(1000)
                Log.e("NewsAnalysisRepository", "无法解析AI响应为JSON，返回降级结果")
                return NewsAnalysis(
                    id = id,
                    newsContent = newsContent,
                    summary = jsonString.take(500),
                    sentiment = NewsAnalysis.Sentiment.NEUTRAL,
                    confidence = 0.5,
                    keyPoints = emptyList(),
                    affectedSectors = emptyList(),
                    recommendedStocks = emptyList(),
                    riskWarnings = emptyList(),
                    recommendations = emptyList(),
                    analysisType = options.analysisType,
                    createdAt = System.currentTimeMillis(),
                    metadata = mapOf(
                        "source" to "qianfan_api",
                        "status" to "PARSE_ERROR",
                        "parse_error" to "cannot_parse_json",
                        "original_response_excerpt" to excerpt
                    )
                )
            }

            val summary = (json["summary"] as? String)
                ?: (json["summaryText"] as? String)
                ?: (json["abstract"] as? String)
                ?: cleaned.take(200)

            val sentimentStr = (json["sentiment"] as? String) ?: "NEUTRAL"
            val confidence = ((json["confidence"] as? Number)?.toDouble() ?: 0.5).coerceIn(0.0, 1.0)

            val keyPoints = mutableListOf<String>()
            @Suppress("UNCHECKED_CAST")
            (json["keyPoints"] as? List<*>)?.forEach { if (it is String) keyPoints.add(it) }
            @Suppress("UNCHECKED_CAST")
            (json["key_points"] as? List<*>)?.forEach { if (it is String) keyPoints.add(it) }

            val recommendations = mutableListOf<String>()
            @Suppress("UNCHECKED_CAST")
            (json["recommendations"] as? List<*>)?.forEach { if (it is String) recommendations.add(it) }
            @Suppress("UNCHECKED_CAST")
            (json["advice"] as? List<*>)?.forEach { if (it is String) recommendations.add(it) }

            val affectedSectors = mutableListOf<AffectedSector>()
            @Suppress("UNCHECKED_CAST")
            val rawSectors = (json["affectedSectors"] as? List<*>) ?: (json["affected_sectors"] as? List<*>)
            if (rawSectors != null) {
                rawSectors.forEach { item ->
                    @Suppress("UNCHECKED_CAST")
                    val sectorObj = item as? Map<String, Any?> ?: return@forEach
                    val sectorName = (sectorObj["sectorName"] as? String)
                        ?: (sectorObj["name"] as? String) ?: ""
                    val impactLevelStr = (sectorObj["impactLevel"] as? String) ?: "MEDIUM"
                    val impactLevel = when (impactLevelStr.uppercase()) {
                        "HIGH" -> AffectedSector.ImpactLevel.HIGH
                        "LOW" -> AffectedSector.ImpactLevel.LOW
                        else -> AffectedSector.ImpactLevel.MEDIUM
                    }
                    val impactDescription = (sectorObj["impactDescription"] as? String) ?: ""
                    if (sectorName.isNotBlank()) {
                        affectedSectors.add(
                            AffectedSector(
                                sectorCode = sectorName.lowercase().replace(" ", "_"),
                                sectorName = sectorName,
                                impactLevel = impactLevel,
                                impactDescription = impactDescription,
                                relatedStocks = emptyList()
                            )
                        )
                    }
                }
            }

            val recommendedStocks = mutableListOf<RecommendedStock>()
            @Suppress("UNCHECKED_CAST")
            val rawStocks = (json["recommendedStocks"] as? List<*>) ?: (json["recommended_stocks"] as? List<*>)
            if (rawStocks != null) {
                rawStocks.forEach { item ->
                    @Suppress("UNCHECKED_CAST")
                    val stockObj = item as? Map<String, Any?> ?: return@forEach
                    val stockCode = (stockObj["stockCode"] as? String) ?: (stockObj["code"] as? String) ?: ""
                    val stockName = (stockObj["stockName"] as? String) ?: (stockObj["name"] as? String) ?: ""
                    val market = (stockObj["market"] as? String) ?: "SH"
                    val sectorName = (stockObj["sectorName"] as? String)?.takeIf { it.isNotBlank() }
                    val reason = (stockObj["reason"] as? String) ?: ""
                    val recommendationStr = (stockObj["recommendation"] as? String) ?: "WATCH"
                    val stockConfidence = ((stockObj["confidence"] as? Number)?.toDouble() ?: 0.7).coerceIn(0.0, 1.0)
                    val targetPrice = (stockObj["targetPrice"] as? Number)?.toDouble()?.takeIf { it > 0 }

                    val recommendation = when (recommendationStr.uppercase()) {
                        "BUY" -> RecommendedStock.RecommendationType.BUY
                        "HOLD" -> RecommendedStock.RecommendationType.HOLD
                        else -> RecommendedStock.RecommendationType.WATCH
                    }

                    recommendedStocks.add(
                        RecommendedStock(
                            stockCode = stockCode,
                            stockName = stockName,
                            market = market,
                            recommendation = recommendation,
                            reason = reason,
                            confidence = stockConfidence,
                            targetPrice = targetPrice,
                            riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                            sectorName = sectorName
                        )
                    )
                }
            }

            val metadata = mutableMapOf<String, String>("source" to "qianfan_api")

            if (affectedSectors.isEmpty()) {
                metadata["affectedSectors_status"] = "EMPTY"
                try {
                    val frag = extractJsonFragment(cleaned, "affectedSectors") ?: extractJsonFragment(cleaned, "affected_sectors")
                    if (!frag.isNullOrBlank()) {
                        val maybe = tryParseToMap("{\"x\":$frag}")
                        @Suppress("UNCHECKED_CAST")
                        val arr = maybe?.get("x") as? List<*>
                        if (!arr.isNullOrEmpty()) {
                            @Suppress("UNCHECKED_CAST")
                            (arr as List<Map<String, Any?>>).forEach { sectorObj ->
                                val sectorName = (sectorObj["sectorName"] as? String) ?: (sectorObj["name"] as? String) ?: ""
                                if (sectorName.isNotBlank()) {
                                    affectedSectors.add(
                                        AffectedSector(
                                            sectorCode = sectorName.lowercase().replace(" ", "_"),
                                            sectorName = sectorName,
                                            impactLevel = AffectedSector.ImpactLevel.MEDIUM,
                                            impactDescription = (sectorObj["impactDescription"] as? String) ?: "",
                                            relatedStocks = emptyList()
                                        )
                                    )
                                }
                            }
                            metadata["affectedSectors_status"] = "RECOVERED_FROM_FRAGMENT"
                        }
                    }
                } catch (e: Exception) {
                    Log.w("NewsAnalysisRepository", "尝试从片段恢复 affectedSectors 失败: ${e.message}")
                }
            } else {
                metadata["affectedSectors_status"] = "OK"
            }

            if (recommendedStocks.isEmpty()) {
                metadata["recommendedStocks_status"] = "EMPTY"
                try {
                    val frag = extractJsonFragment(cleaned, "recommendedStocks") ?: extractJsonFragment(cleaned, "recommended_stocks")
                    if (!frag.isNullOrBlank()) {
                        val maybe = tryParseToMap("{\"x\":$frag}")
                        @Suppress("UNCHECKED_CAST")
                        val arr = maybe?.get("x") as? List<*>
                        if (!arr.isNullOrEmpty()) {
                            @Suppress("UNCHECKED_CAST")
                            (arr as List<Map<String, Any?>>).forEach { stockObj ->
                                val stockCode = (stockObj["stockCode"] as? String) ?: (stockObj["code"] as? String) ?: ""
                                val stockName = (stockObj["stockName"] as? String) ?: (stockObj["name"] as? String) ?: ""
                                if (stockCode.isNotBlank() || stockName.isNotBlank()) {
                                    recommendedStocks.add(
                                        RecommendedStock(
                                            stockCode = stockCode,
                                            stockName = stockName,
                                            market = (stockObj["market"] as? String) ?: "SH",
                                            recommendation = RecommendedStock.RecommendationType.WATCH,
                                            reason = (stockObj["reason"] as? String) ?: "",
                                            confidence = ((stockObj["confidence"] as? Number)?.toDouble() ?: 0.7).coerceIn(0.0, 1.0),
                                            targetPrice = (stockObj["targetPrice"] as? Number)?.toDouble()?.takeIf { it > 0 },
                                            riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                                            sectorName = (stockObj["sectorName"] as? String)
                                        )
                                    )
                                }
                            }
                            metadata["recommendedStocks_status"] = "RECOVERED_FROM_FRAGMENT"
                        }
                    }
                } catch (e: Exception) {
                    Log.w("NewsAnalysisRepository", "尝试从片段恢复 recommendedStocks 失败: ${e.message}")
                }
            } else {
                metadata["recommendedStocks_status"] = "OK"
            }

            if (affectedSectors.isEmpty() && recommendedStocks.isEmpty()) {
                metadata["status"] = "EMPTY_KEYS"
                metadata["parse_warning"] = "both_affectedSectors_and_recommendedStocks_empty"
                Log.w("NewsAnalysisRepository", "解析后关键字段空：affectedSectors & recommendedStocks 都为空")
            }

            return NewsAnalysis(
                id = id,
                newsContent = newsContent,
                summary = summary,
                sentiment = when (sentimentStr.uppercase()) {
                    "POSITIVE" -> NewsAnalysis.Sentiment.POSITIVE
                    "NEGATIVE" -> NewsAnalysis.Sentiment.NEGATIVE
                    else -> NewsAnalysis.Sentiment.NEUTRAL
                },
                confidence = confidence,
                keyPoints = keyPoints,
                affectedSectors = affectedSectors,
                recommendedStocks = recommendedStocks,
                riskWarnings = (json["riskWarnings"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                recommendations = recommendations,
                analysisType = options.analysisType,
                createdAt = System.currentTimeMillis(),
                metadata = metadata
            )
        } catch (e: Exception) {
            Log.e("NewsAnalysisRepository", "parseQianfanResponse 未捕获异常: ${e.message}", e)
            return NewsAnalysis(
                id = id,
                newsContent = newsContent,
                summary = jsonString.take(500),
                sentiment = NewsAnalysis.Sentiment.NEUTRAL,
                confidence = 0.5,
                keyPoints = emptyList(),
                affectedSectors = emptyList(),
                recommendedStocks = emptyList(),
                riskWarnings = emptyList(),
                recommendations = emptyList(),
                analysisType = options.analysisType,
                createdAt = System.currentTimeMillis(),
                metadata = mapOf(
                    "source" to "qianfan_api",
                    "status" to "PARSE_ERROR",
                    "parse_error" to (e.message ?: "unknown"),
                    "original_response_excerpt" to jsonString.take(1000)
                )
            )
        }
    }
    
    private fun AIAnalysisCacheEntity.toDomainModel(): NewsAnalysis? {
        return try {
            val jsonAdapter = moshi.adapter(NewsAnalysis::class.java)
            jsonAdapter.fromJson(content) ?: return null
        } catch (e: Exception) {
            null
        }
    }
    
    private fun NewsAnalysis.toEntity(): AIAnalysisCacheEntity {
        val jsonAdapter = moshi.adapter(NewsAnalysis::class.java)
        val contentJson = jsonAdapter.toJson(this) ?: "{}"
        
        return AIAnalysisCacheEntity.create(
            cacheKey = id,
            analysisType = "NEWS",
            content = contentJson,
            summary = summary,
            confidence = confidence,
            expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24小时后过期
        )
    }
    
}
