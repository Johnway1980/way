package com.alphadoer.trader.domain.repository

import com.alphadoer.trader.domain.model.review.DailyReviewSummary
import com.alphadoer.trader.domain.model.review.ImprovementPlan
import com.alphadoer.trader.domain.model.review.MarketReview
import com.alphadoer.trader.domain.model.review.MistakePattern
import com.alphadoer.trader.domain.model.review.MistakeStatistics
import com.alphadoer.trader.domain.model.review.StockDeepAnalysis
import com.alphadoer.trader.domain.model.review.StockLeaderboard
import com.alphadoer.trader.domain.model.review.TradeMistake
import kotlinx.coroutines.flow.Flow

/**
 * 复盘Repository接口
 */
interface ReviewRepository {
    
    // ========== 市场复盘 ==========
    suspend fun getMarketReview(date: String): MarketReview?
    suspend fun saveMarketReview(review: MarketReview): Result<Unit>
    suspend fun getStockLeaderboard(date: String): StockLeaderboard?
    
    // ========== 个股分析 ==========
    suspend fun getStockDeepAnalysis(stockCode: String, date: String): StockDeepAnalysis?
    suspend fun saveStockDeepAnalysis(analysis: StockDeepAnalysis): Result<Unit>
    
    // ========== 错误分析 ==========
    suspend fun saveTradeMistake(mistake: TradeMistake): Result<Unit>
    suspend fun getTradeMistakesByDate(date: String): List<TradeMistake>
    suspend fun getTradeMistakeById(id: String): TradeMistake?
    suspend fun getAllMistakePatterns(): List<MistakePattern>
    suspend fun saveMistakePattern(pattern: MistakePattern): Result<Unit>
    suspend fun getMistakeStatistics(dateRange: String): MistakeStatistics
    
    // ========== 复盘总结 ==========
    suspend fun getDailyReviewSummary(date: String): DailyReviewSummary?
    suspend fun saveDailyReviewSummary(summary: DailyReviewSummary): Result<Unit>
    suspend fun getImprovementPlan(id: String): ImprovementPlan?
    suspend fun saveImprovementPlan(plan: ImprovementPlan): Result<Unit>
    suspend fun getActiveImprovementPlans(): Flow<List<ImprovementPlan>>
}
