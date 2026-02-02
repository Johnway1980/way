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
import com.alphadoer.trader.presentation.morningreading.MorningReadingEvent
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest

class MorningReadingViewModelLinkTest {

    // Use real use case classes with a fake repository instance when needed

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
        override suspend fun getStockByCode(code: String): RecommendedStock? = null
        override suspend fun searchStocks(query: String): List<RecommendedStock> = emptyList()
        override fun getFavoriteStocks(): Flow<List<RecommendedStock>> = flowOf(emptyList())
        override suspend fun addToFavorites(stockCode: String): Result<Unit> = Result.success(Unit)
        override suspend fun removeFromFavorites(stockCode: String): Result<Unit> = Result.success(Unit)
        override suspend fun saveStock(stock: RecommendedStock): Result<Unit> = Result.success(Unit)
    }

    private class FakeTradeJournalRepository : TradeJournalRepository {
        override fun getAllJournals(): Flow<List<com.alphadoer.trader.domain.model.TradeJournal>> = flowOf(emptyList())
        override suspend fun getJournalByDate(date: String): com.alphadoer.trader.domain.model.TradeJournal? = null
        override fun getJournalByDateFlow(date: String): Flow<com.alphadoer.trader.domain.model.TradeJournal?> = flowOf(null)
        override suspend fun getJournalsByDateRange(startDate: String, endDate: String): List<com.alphadoer.trader.domain.model.TradeJournal> = emptyList()
        override fun getJournalsByReviewStatus(completed: Boolean): Flow<List<com.alphadoer.trader.domain.model.TradeJournal>> = flowOf(emptyList())
        override suspend fun insertJournal(journal: com.alphadoer.trader.domain.model.TradeJournal) { /* no-op */ }
        override suspend fun updateJournal(journal: com.alphadoer.trader.domain.model.TradeJournal) { /* no-op */ }
        override suspend fun updateReviewStatus(date: String, completed: Boolean) { /* no-op */ }
        override suspend fun deleteJournal(date: String) { /* no-op */ }
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
    fun linkSectorsFromAnalysis_writesTop3_withAtLeast5StocksEach() = runTest {
        try {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        } catch (e: Throwable) {
            e.printStackTrace()
            org.junit.Assert.fail("Dispatchers.setMain failed: ${'$'}{e.message}")
        }
        try {
        val sectors = listOf(
            AffectedSector("industrial_internet", "工业互联网", AffectedSector.ImpactLevel.HIGH, "工业数据相关", emptyList()),
            AffectedSector("ai", "人工智能", AffectedSector.ImpactLevel.MEDIUM, "AI应用落地", emptyList()),
            AffectedSector("semi", "半导体", AffectedSector.ImpactLevel.LOW, "设备国产替代", emptyList())
        )

        val recs = listOf(
            // 工业互联网 2只（不足5，需补齐）
            RecommendedStock(
                stockCode = "300166",
                stockName = "东方国信",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "工业互联网平台",
                confidence = 0.7,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "工业互联网"
            ),
            RecommendedStock(
                stockCode = "002410",
                stockName = "广联达",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "工业软件",
                confidence = 0.7,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "工业互联网"
            ),
            // 人工智能 3只（不足5，需补齐）
            RecommendedStock(
                stockCode = "002230",
                stockName = "科大讯飞",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "AI语音",
                confidence = 0.7,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "人工智能"
            ),
            RecommendedStock(
                stockCode = "000977",
                stockName = "浪潮信息",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "AI服务器",
                confidence = 0.7,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "人工智能"
            ),
            RecommendedStock(
                stockCode = "300496",
                stockName = "中科创达",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "智能操作系统",
                confidence = 0.7,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "人工智能"
            ),
            // 半导体 1只（不足5，需补齐）
            RecommendedStock(
                stockCode = "600584",
                stockName = "长电科技",
                market = "SH",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "封测龙头",
                confidence = 0.7,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "半导体"
            )
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
            analysisType = com.alphadoer.trader.domain.model.NewsAnalysis.AnalysisType.QUICK,
            createdAt = System.currentTimeMillis(),
            metadata = emptyMap()
        )

        val sectorRepo = CapturingSectorSelectionRepository()
        val fakeRepo = FakeNewsAnalysisRepository(analysis)
        val vm = MorningReadingViewModel(
            analyzeNewsUseCase = com.alphadoer.trader.domain.usecase.AnalyzeNewsUseCase(fakeRepo),
            getAnalysisHistoryUseCase = com.alphadoer.trader.domain.usecase.GetAnalysisHistoryUseCase(fakeRepo),
            saveAnalysisResultUseCase = com.alphadoer.trader.domain.usecase.SaveAnalysisResultUseCase(fakeRepo),
            newsAnalysisRepository = fakeRepo,
            stockRepository = FakeStockRepository(),
            tradeJournalRepository = FakeTradeJournalRepository(),
            sectorSelectionRepository = sectorRepo
        )

        // 触发事件：从分析写入板块记录
        try {
            vm.handleEvent(MorningReadingEvent.LinkSectorsFromAnalysis(analysis.id))
        } catch (e: Throwable) {
            e.printStackTrace()
            org.junit.Assert.fail("vm.handleEvent failed: ${'$'}{e.message}")
        }

        // 等待协程执行完成
        testScheduler.advanceUntilIdle()

        // 如果未写入预期数量，抛出带上下文的异常以便诊断
        if (sectorRepo.saved.size < 3) {
            val state = vm.uiState.value
            org.junit.Assert.fail("板块写入不足: saved=${'$'}{sectorRepo.saved.size}, savedList=${'$'}{sectorRepo.saved}, uiError=${'$'}{state.errorMessage}, currentAnalysis=${'$'}{state.currentAnalysis}")
        }

        // 校验：应写入TOP3三个板块
        assertEquals(3, sectorRepo.saved.size)

        // 每个板块至少5只股票
        assertTrue(sectorRepo.saved.all { it.stockCodes.size >= 5 })

        // 板块名称匹配
        val names = sectorRepo.saved.map { it.sectorName }.toSet()
        assertTrue(names.contains("工业互联网"))
        assertTrue(names.contains("人工智能"))
        assertTrue(names.contains("半导体"))
        } finally {
            Dispatchers.resetMain()
        }
    }
}
