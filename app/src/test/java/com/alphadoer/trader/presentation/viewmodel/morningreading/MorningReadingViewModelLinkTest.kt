package com.alphadoer.trader.presentation.viewmodel.morningreading

import com.alphadoer.trader.domain.model.AffectedSector
import com.alphadoer.trader.domain.model.NewsAnalysis
import com.alphadoer.trader.domain.model.RecommendedStock
import com.alphadoer.trader.domain.repository.NewsAnalysisRepository
import com.alphadoer.trader.domain.repository.SectorSelectionRepository
import com.alphadoer.trader.domain.repository.StockRepository
import com.alphadoer.trader.domain.repository.TradeJournalRepository
import com.alphadoer.trader.domain.usecase.AnalyzeNewsUseCase
import com.alphadoer.trader.domain.usecase.GetAnalysisHistoryUseCase
import com.alphadoer.trader.domain.usecase.SaveAnalysisResultUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MorningReadingViewModelLinkTest {

    private class FakeAnalyzeNewsUseCase : AnalyzeNewsUseCase({ _, _ -> Result.failure(Exception("not used")) })
    private class FakeGetAnalysisHistoryUseCase : GetAnalysisHistoryUseCase({ flowOf(emptyList()) })
    private class FakeSaveAnalysisResultUseCase : SaveAnalysisResultUseCase({ Result.success(Unit) })

    private class FakeNewsAnalysisRepository(private val analysis: NewsAnalysis) : NewsAnalysisRepository {
        override suspend fun analyzeNews(newsContent: String, options: com.alphadoer.trader.domain.model.AnalysisOptions): Result<NewsAnalysis> =
            Result.success(analysis)
        override fun getAnalysisHistory(): Flow<List<NewsAnalysis>> = flowOf(emptyList())
        override suspend fun getAnalysisById(id: String): NewsAnalysis? = analysis
        override suspend fun saveAnalysis(analysis: NewsAnalysis): Result<Unit> = Result.success(Unit)
        override suspend fun deleteAnalysis(id: String): Result<Unit> = Result.success(Unit)
        override suspend fun getCachedAnalysis(newsContent: String): NewsAnalysis? = null
    }

    private class FakeStockRepository : StockRepository {
        override suspend fun getStockByCode(code: String) = null
        override suspend fun saveStock(stock: RecommendedStock): Result<Unit> = Result.success(Unit)
        override suspend fun addToFavorites(code: String): Result<Unit> = Result.success(Unit)
        override fun getFavorites(): Flow<List<RecommendedStock>> = flowOf(emptyList())
    }

    private class FakeTradeJournalRepository : TradeJournalRepository {
        override suspend fun getJournalByDate(date: String) = null
        override suspend fun insertJournal(journal: com.alphadoer.trader.domain.model.TradeJournal): Result<Unit> = Result.success(Unit)
        override suspend fun updateJournal(journal: com.alphadoer.trader.domain.model.TradeJournal): Result<Unit> = Result.success(Unit)
        override fun getAllJournals(): Flow<List<com.alphadoer.trader.domain.model.TradeJournal>> = flowOf(emptyList())
    }

    private class CapturingSectorSelectionRepository : SectorSelectionRepository {
        val saved = mutableListOf<com.alphadoer.trader.domain.model.SectorSelectionRecord>()
        override suspend fun saveSelection(record: com.alphadoer.trader.domain.model.SectorSelectionRecord): Result<Unit> {
            saved.add(record)
            return Result.success(Unit)
        }
        override suspend fun deleteSelection(id: String): Result<Unit> = Result.success(Unit)
        override fun getSelectionsByDate(date: String): Flow<List<com.alphadoer.trader.domain.model.SectorSelectionRecord>> = flowOf(emptyList())
    }

    @Test
    fun linkSectorsFromAnalysis_writesTop3_withAtLeast5StocksEach() {
        val sectors = listOf(
            AffectedSector("industrial_internet", "工业互联网", AffectedSector.ImpactLevel.HIGH, "工业数据相关", emptyList()),
            AffectedSector("ai", "人工智能", AffectedSector.ImpactLevel.MEDIUM, "AI应用落地", emptyList()),
            AffectedSector("semi", "半导体", AffectedSector.ImpactLevel.LOW, "设备国产替代", emptyList())
        )

        val recs = listOf(
            // 工业互联网 2只（不足5，需补齐）
            RecommendedStock("300166", "东方国信", "SZ", RecommendedStock.RecommendationType.WATCH, "工业互联网平台", 0.7, RecommendedStock.RiskLevel.MEDIUM, "工业互联网"),
            RecommendedStock("002410", "广联达", "SZ", RecommendedStock.RecommendationType.WATCH, "工业软件", 0.7, RecommendedStock.RiskLevel.MEDIUM, "工业互联网"),
            // 人工智能 3只（不足5，需补齐）
            RecommendedStock("002230", "科大讯飞", "SZ", RecommendedStock.RecommendationType.WATCH, "AI语音", 0.7, RecommendedStock.RiskLevel.MEDIUM, "人工智能"),
            RecommendedStock("000977", "浪潮信息", "SZ", RecommendedStock.RecommendationType.WATCH, "AI服务器", 0.7, RecommendedStock.RiskLevel.MEDIUM, "人工智能"),
            RecommendedStock("300496", "中科创达", "SZ", RecommendedStock.RecommendationType.WATCH, "智能操作系统", 0.7, RecommendedStock.RiskLevel.MEDIUM, "人工智能"),
            // 半导体 1只（不足5，需补齐）
            RecommendedStock("600584", "长电科技", "SH", RecommendedStock.RecommendationType.WATCH, "封测龙头", 0.7, RecommendedStock.RiskLevel.MEDIUM, "半导体")
        )

        val analysis = NewsAnalysis(
            id = "analysis-test-001",
            newsContent = "测试新闻",
            summary = "摘要",
            sentiment = NewsAnalysis.Sentiment.NEUTRAL,
            confidence = 0.5,
            keyPoints = emptyList(),
            affectedSectors = sectors,
            recommendedStocks = recs,
            riskWarnings = emptyList(),
            recommendations = emptyList(),
            analysisType = com.alphadoer.trader.domain.model.AnalysisOptions.AnalysisType.NEWS,
            createdAt = System.currentTimeMillis(),
            metadata = emptyMap()
        )

        val sectorRepo = CapturingSectorSelectionRepository()
        val vm = MorningReadingViewModel(
            analyzeNewsUseCase = FakeAnalyzeNewsUseCase(),
            getAnalysisHistoryUseCase = FakeGetAnalysisHistoryUseCase(),
            saveAnalysisResultUseCase = FakeSaveAnalysisResultUseCase(),
            newsAnalysisRepository = FakeNewsAnalysisRepository(analysis),
            stockRepository = FakeStockRepository(),
            tradeJournalRepository = FakeTradeJournalRepository(),
            sectorSelectionRepository = sectorRepo
        )

        // 触发事件：从分析写入板块记录
        vm.handleEvent(MorningReadingEvent.LinkSectorsFromAnalysis(analysis.id))

        // 校验：应写入TOP3三个板块
        assertEquals(3, sectorRepo.saved.size)

        // 每个板块至少5只股票
        assertTrue(sectorRepo.saved.all { it.stockCodes.size >= 5 })

        // 板块名称匹配
        val names = sectorRepo.saved.map { it.sectorName }.toSet()
        assertTrue(names.contains("工业互联网"))
        assertTrue(names.contains("人工智能"))
        assertTrue(names.contains("半导体"))
    }
}
