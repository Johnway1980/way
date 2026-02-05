package com.alphadoer.trader.data.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.io.File

class RecoveryAndCompensationTest {

    @Test
    fun inferSectorsAndEnsureRecommendations() = runBlocking {
        // load raw sample (same logic as other tests)
        val resourceStream = javaClass.classLoader.getResourceAsStream("ai_inner_unescaped_raw.txt")
        val tmp = File.createTempFile("ai_raw_test", ".txt")
        tmp.writeText(resourceStream.bufferedReader().use { it.readText() })
        val raw = tmp.readText()

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

        val parsed = repo.parseQianfanResponseForTest(raw, raw, com.alphadoer.trader.domain.model.AnalysisOptions())

        // Debug output
        println("parsed affectedSectors: ${parsed.affectedSectors}")
        println("parsed recommendedStocks.count=${parsed.recommendedStocks.size}")

        // Expect we inferred at least one sector for this AI/space news
        Assert.assertTrue("affectedSectors should not be empty after inference", parsed.affectedSectors.isNotEmpty())

        // For each sector ensure at least 3 related stocks
        parsed.affectedSectors.forEach { sector ->
            println("sector ${sector.sectorName} relatedStocks=${sector.relatedStocks}")
            Assert.assertTrue("sector ${sector.sectorName} should have >=3 relatedStocks", sector.relatedStocks.size >= 3)
        }

        // Also ensure recommendedStocks contains at least total sectors * 3
        val totalNeeded = parsed.affectedSectors.size * 3
        Assert.assertTrue("recommendedStocks should have at least $totalNeeded entries", parsed.recommendedStocks.size >= totalNeeded)
    }
}
