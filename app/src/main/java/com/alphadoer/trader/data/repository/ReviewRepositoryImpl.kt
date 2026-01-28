package com.alphadoer.trader.data.repository

import com.alphadoer.trader.data.local.dao.TradeMistakeDao
import com.alphadoer.trader.data.local.entity.TradeMistakeEntity
import com.alphadoer.trader.domain.model.review.DailyReviewSummary
import com.alphadoer.trader.domain.model.review.ImprovementPlan
import com.alphadoer.trader.domain.model.review.MarketReview
import com.alphadoer.trader.domain.model.review.MistakePattern
import com.alphadoer.trader.domain.model.review.MistakeStatistics
import com.alphadoer.trader.domain.model.review.StockDeepAnalysis
import com.alphadoer.trader.domain.model.review.StockLeaderboard
import com.alphadoer.trader.domain.model.review.TradeMistake
import com.alphadoer.trader.domain.repository.ReviewRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import javax.inject.Inject

/**
 * 复盘Repository实现
 */
class ReviewRepositoryImpl @Inject constructor(
    private val tradeMistakeDao: TradeMistakeDao
) : ReviewRepository {
    
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    
    // ========== 市场复盘 ==========
    override suspend fun getMarketReview(date: String): MarketReview? {
        // TODO: 实现从数据源获取
        return null
    }
    
    override suspend fun saveMarketReview(review: MarketReview): Result<Unit> {
        // TODO: 实现保存
        return Result.success(Unit)
    }
    
    override suspend fun getStockLeaderboard(date: String): StockLeaderboard? {
        // TODO: 实现从数据源获取
        return null
    }
    
    // ========== 个股分析 ==========
    override suspend fun getStockDeepAnalysis(stockCode: String, date: String): StockDeepAnalysis? {
        // TODO: 实现从数据源获取
        return null
    }
    
    override suspend fun saveStockDeepAnalysis(analysis: StockDeepAnalysis): Result<Unit> {
        // TODO: 实现保存
        return Result.success(Unit)
    }
    
    // ========== 错误分析 ==========
    override suspend fun saveTradeMistake(mistake: TradeMistake): Result<Unit> {
        return try {
            val entity = mistake.toEntity()
            tradeMistakeDao.insertMistake(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTradeMistakesByDate(date: String): List<TradeMistake> {
        val entities = tradeMistakeDao.getMistakesByDate(date)
        return entities.map { it.toDomainModel() }
    }
    
    override suspend fun getTradeMistakeById(id: String): TradeMistake? {
        val entity = tradeMistakeDao.getMistakeById(id)
        return entity?.toDomainModel()
    }
    
    override suspend fun getAllMistakePatterns(): List<MistakePattern> {
        // TODO: 实现从数据源获取
        return emptyList()
    }
    
    override suspend fun saveMistakePattern(pattern: MistakePattern): Result<Unit> {
        // TODO: 实现保存
        return Result.success(Unit)
    }
    
    override suspend fun getMistakeStatistics(dateRange: String): MistakeStatistics {
        // TODO: 实现统计计算
        return MistakeStatistics(
            dateRange = dateRange,
            totalMistakes = 0,
            totalLoss = 0.0,
            mistakeByType = emptyMap(),
            mistakeByCategory = emptyMap(),
            timeDistribution = emptyMap(),
            topMistakes = emptyList(),
            improvementTrend = null
        )
    }
    
    // ========== 复盘总结 ==========
    override suspend fun getDailyReviewSummary(date: String): DailyReviewSummary? {
        // TODO: 实现从数据源获取
        return null
    }
    
    override suspend fun saveDailyReviewSummary(summary: DailyReviewSummary): Result<Unit> {
        // TODO: 实现保存
        return Result.success(Unit)
    }
    
    override suspend fun getImprovementPlan(id: String): ImprovementPlan? {
        // TODO: 实现从数据源获取
        return null
    }
    
    override suspend fun saveImprovementPlan(plan: ImprovementPlan): Result<Unit> {
        // TODO: 实现保存
        return Result.success(Unit)
    }
    
    override suspend fun getActiveImprovementPlans(): kotlinx.coroutines.flow.Flow<List<ImprovementPlan>> {
        // TODO: 实现从数据源获取
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }
    
    // ========== 数据转换 ==========
    private fun TradeMistake.toEntity(): TradeMistakeEntity {
        val contextAdapter = moshi.adapter(TradeMistake.MistakeContext::class.java)
        val listStringAdapter = moshi.adapter<List<String>>(
            Types.newParameterizedType(List::class.java, String::class.java)
        )
        
        return TradeMistakeEntity(
            id = id,
            tradeRecordId = tradeRecordId,
            date = date,
            mistakeType = mistakeType.name,
            category = category.name,
            description = description,
            rootCause = rootCause,
            impactAmount = impactAmount,
            contextJson = contextAdapter.toJson(context),
            improvementMeasuresJson = listStringAdapter.toJson(improvementMeasures),
            relatedMistakesJson = listStringAdapter.toJson(relatedMistakes),
            createdAt = createdAt
        )
    }
    
    private fun TradeMistakeEntity.toDomainModel(): TradeMistake {
        val contextAdapter = moshi.adapter(TradeMistake.MistakeContext::class.java)
        val listStringAdapter = moshi.adapter<List<String>>(
            Types.newParameterizedType(List::class.java, String::class.java)
        )
        
        return TradeMistake(
            id = id,
            tradeRecordId = tradeRecordId,
            date = date,
            mistakeType = TradeMistake.MistakeType.valueOf(mistakeType),
            category = TradeMistake.MistakeCategory.valueOf(category),
            description = description,
            rootCause = rootCause,
            impactAmount = impactAmount,
            context = contextAdapter.fromJson(contextJson) ?: TradeMistake.MistakeContext(
                marketEnvironment = "",
                emotionState = "",
                timeOfDay = "",
                stockCode = "",
                notes = null
            ),
            improvementMeasures = listStringAdapter.fromJson(improvementMeasuresJson) ?: emptyList(),
            relatedMistakes = listStringAdapter.fromJson(relatedMistakesJson) ?: emptyList(),
            createdAt = createdAt
        )
    }
}
