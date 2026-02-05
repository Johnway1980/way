package com.alphadoer.trader.data.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.io.File

class CandidatePoolConfigTest {

    @Test
    fun csvConfigIsUsedForSynthesis() = runBlocking {
        // Ensure resource is present on classpath
        val res = javaClass.classLoader.getResourceAsStream("synth_candidate_pool.csv")
        Assert.assertNotNull("synth_candidate_pool.csv must be on classpath for this test", res)

        val raw = javaClass.classLoader.getResourceAsStream("ai_inner_unescaped_raw.txt")?.bufferedReader()?.use { it.readText() }
            ?: ""

        val repo = NewsAnalysisRepositoryImpl(
            aiService = object : com.alphadoer.trader.data.remote.api.AIService {
                override suspend fun analyzeNews(request: com.alphadoer.trader.data.remote.dto.QianfanChatRequest): com.alphadoer.trader.data.remote.dto.QianfanChatResponse {
                    throw UnsupportedOperationException()
                }

                override suspend fun analyzeMarket(request: com.alphadoer.trader.data.remote.dto.QianfanChatRequest): com.alphadoer.trader.data.remote.dto.QianfanChatResponse {
                    throw UnsupportedOperationException()
                }

                override suspend fun analyzeStock(request: com.alphadoer.trader.data.remote.dto.QianfanChatRequest): com.alphadoer.trader.data.remote.dto.QianfanChatResponse {
                    throw UnsupportedOperationException()
                }
            },
            aiAnalysisCacheDao = object : com.alphadoer.trader.data.local.dao.AIAnalysisCacheDao {
                override suspend fun getCacheByKey(key: String) = null
                override fun getCacheByKeyFlow(key: String): kotlinx.coroutines.flow.Flow<com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity?> { throw UnsupportedOperationException() }
                override suspend fun getCachesByType(type: String) = emptyList<com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity>()
                override fun getCachesByTypeFlow(type: String): kotlinx.coroutines.flow.Flow<List<com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity>> { throw UnsupportedOperationException() }
                override suspend fun getValidCaches(currentTime: Long) = emptyList<com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity>()
                override suspend fun deleteExpiredCaches(currentTime: Long) {}
                override suspend fun deleteCacheByKey(key: String) {}
                override suspend fun deleteCachesByType(type: String) {}
                override suspend fun insertCache(cache: com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity) {}
                override suspend fun insertCaches(caches: List<com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity>) {}
                override suspend fun updateCache(cache: com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity) {}
                override suspend fun deleteCache(cache: com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity) {}
                override suspend fun getCacheCount(): Int = 0
            },
            stockRecommendationValidator = com.alphadoer.trader.data.util.StockRecommendationValidator()
        )

        val parsed = repo.parseQianfanResponseForTest(raw ?: "", raw ?: "", com.alphadoer.trader.domain.model.AnalysisOptions())

        // The first code from CSV is 300750; synthesized recommendedStocks should include it when fallback occurs
        val codes = parsed.recommendedStocks.map { it.stockCode }
        Assert.assertTrue("synthesized pool first entry should appear in recommendedStocks", codes.contains("300750"))
    }
}
