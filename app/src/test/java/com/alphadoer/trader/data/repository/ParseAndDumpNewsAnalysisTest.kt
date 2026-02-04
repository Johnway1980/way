package com.alphadoer.trader.data.repository

import com.alphadoer.trader.data.local.dao.AIAnalysisCacheDao
import com.alphadoer.trader.data.remote.api.AIService
import com.alphadoer.trader.domain.model.AnalysisOptions
import com.alphadoer.trader.data.repository.NewsAnalysisRepositoryImpl
import com.alphadoer.trader.data.util.StockRecommendationValidator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import kotlinx.coroutines.flow.Flow

class ParseAndDumpNewsAnalysisTest {

    // Minimal dummy implementations to satisfy constructor
    private val dummyAIService = object : AIService {
        override suspend fun analyzeNews(request: com.alphadoer.trader.data.remote.dto.QianfanChatRequest): com.alphadoer.trader.data.remote.dto.QianfanChatResponse {
            throw UnsupportedOperationException("not used in test")
        }

        override suspend fun analyzeMarket(request: com.alphadoer.trader.data.remote.dto.QianfanChatRequest): com.alphadoer.trader.data.remote.dto.QianfanChatResponse {
            throw UnsupportedOperationException("not used in test")
        }

        override suspend fun analyzeStock(request: com.alphadoer.trader.data.remote.dto.QianfanChatRequest): com.alphadoer.trader.data.remote.dto.QianfanChatResponse {
            throw UnsupportedOperationException("not used in test")
        }
    }

    private val dummyDao = object : AIAnalysisCacheDao {
        override suspend fun getCacheByKey(key: String) = null
        override fun getCacheByKeyFlow(key: String): Flow<com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity?> { throw UnsupportedOperationException() }
        override suspend fun getCachesByType(type: String) = emptyList<com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity>()
        override fun getCachesByTypeFlow(type: String): Flow<List<com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity>> { throw UnsupportedOperationException() }
        override suspend fun getValidCaches(currentTime: Long) = emptyList<com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity>()
        override suspend fun deleteExpiredCaches(currentTime: Long) {}
        override suspend fun deleteCacheByKey(key: String) {}
        override suspend fun deleteCachesByType(type: String) {}
        override suspend fun insertCache(cache: com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity) {}
        override suspend fun insertCaches(caches: List<com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity>) {}
        override suspend fun updateCache(cache: com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity) {}
        override suspend fun deleteCache(cache: com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity) {}
        override suspend fun getCacheCount(): Int = 0
    }

    @Test
    fun dumpParsedNewsAnalysis() = runBlocking {
        // Try to locate the raw AI sample file from several common locations (classpath, project, user)
        fun findRawFile(): File? {
            // try classpath resource first
            val resourceStream = javaClass.classLoader.getResourceAsStream("ai_inner_unescaped_raw.txt")
            if (resourceStream != null) {
                val tmp = File.createTempFile("ai_raw", ".txt")
                tmp.writeText(resourceStream.bufferedReader().use { it.readText() }, Charsets.UTF_8)
                return tmp
            }

            var dir = File(System.getProperty("user.dir"))
            repeat(6) {
                val candidate = File(dir, "app/ai_inner_unescaped_raw.txt")
                if (candidate.exists()) return candidate
                val candidate2 = File(dir, "ai_inner_unescaped_raw.txt")
                if (candidate2.exists()) return candidate2
                dir = dir.parentFile ?: return@repeat
            }

            val userHome = System.getProperty("user.home")
            val fallback = File(userHome, "AndroidStudioProjects/AlphaDoer/app/ai_inner_unescaped_raw.txt")
            if (fallback.exists()) return fallback

            val abs = File("C:/Users/HUAWEI/AndroidStudioProjects/AlphaDoer/app/ai_inner_unescaped_raw.txt")
            if (abs.exists()) return abs

            return null
        }

        val rawFile = findRawFile()
        if (rawFile == null) {
            println("Test resource not found: ai_inner_unescaped_raw.txt (tried classpath and common paths)")
            return@runBlocking
        }

        val raw = rawFile.readText(Charsets.UTF_8)

        val repo = NewsAnalysisRepositoryImpl(
            aiService = dummyAIService,
            aiAnalysisCacheDao = dummyDao,
            stockRecommendationValidator = StockRecommendationValidator()
        )

        val options = AnalysisOptions()

        val parsed = repo.parseQianfanResponseForTest(raw, raw, options)

        // Lightweight JSON serializer for diagnostics (avoid Moshi codegen dependency in unit test)
        fun escapeJson(s: String?): String {
            if (s == null) return "null"
            return '"' + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + '"'
        }

        fun listToJson(list: List<String>?): String {
            if (list == null) return "null"
            return list.joinToString(prefix = "[", postfix = "]") { escapeJson(it) }
        }

        fun affectedSectorToJson(s: com.alphadoer.trader.domain.model.AffectedSector): String {
            return buildString {
                append('{')
                append("\"sectorCode\":${escapeJson(s.sectorCode)},")
                append("\"sectorName\":${escapeJson(s.sectorName)},")
                append("\"impactLevel\":${escapeJson(s.impactLevel.name)},")
                append("\"impactDescription\":${escapeJson(s.impactDescription)},")
                append("\"relatedStocks\":")
                append(listToJson(s.relatedStocks))
                append('}')
            }
        }

        fun recommendedStockToJson(r: com.alphadoer.trader.domain.model.RecommendedStock): String {
            return buildString {
                append('{')
                append("\"stockCode\":${escapeJson(r.stockCode)},")
                append("\"stockName\":${escapeJson(r.stockName)},")
                append("\"market\":${escapeJson(r.market)},")
                append("\"recommendation\":${escapeJson(r.recommendation.name)},")
                append("\"reason\":${escapeJson(r.reason)},")
                append("\"confidence\":${r.confidence},")
                append("\"targetPrice\":")
                append(if (r.targetPrice == null) "null" else r.targetPrice.toString())
                append(',')
                append("\"riskLevel\":${escapeJson(r.riskLevel.name)},")
                append("\"sectorName\":${escapeJson(r.sectorName) }")
                append('}')
            }
        }

        val outDir = File("build/diagnostics")
        outDir.mkdirs()
        val outFile = File(outDir, "newsanalysis-parsed.json")

        val json = buildString {
            append('{')
            append("\"id\":${escapeJson(parsed.id)},")
            append("\"newsContent\":${escapeJson(parsed.newsContent)},")
            append("\"summary\":${escapeJson(parsed.summary)},")
            append("\"sentiment\":${escapeJson(parsed.sentiment.name)},")
            append("\"confidence\":${parsed.confidence},")
            append("\"keyPoints\":")
            append(listToJson(parsed.keyPoints))
            append(',')
            append("\"affectedSectors\":")
            append(parsed.affectedSectors.joinToString(prefix = "[", postfix = "]") { affectedSectorToJson(it) })
            append(',')
            append("\"recommendedStocks\":")
            append(parsed.recommendedStocks.joinToString(prefix = "[", postfix = "]") { recommendedStockToJson(it) })
            append(',')
            append("\"riskWarnings\":")
            append(listToJson(parsed.riskWarnings))
            append(',')
            append("\"recommendations\":")
            append(listToJson(parsed.recommendations))
            append(',')
            append("\"analysisType\":${escapeJson(parsed.analysisType.name)},")
            append("\"createdAt\":${parsed.createdAt},")
            append("\"metadata\":")
            if (parsed.metadata == null) append("null") else {
                append('{')
                append(parsed.metadata.entries.joinToString(",") { (k, v) -> "\"${k}\":${escapeJson(v)}" })
                append('}')
            }
            append('}')
        }

        outFile.writeText(json, Charsets.UTF_8)
        println("Wrote parsed NewsAnalysis to: ${outFile.path}")
        println(json)
    }
}
