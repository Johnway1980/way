package com.alphadoer.trader.domain.repository

import com.alphadoer.trader.domain.model.AnalysisOptions
import com.alphadoer.trader.domain.model.NewsAnalysis
import kotlinx.coroutines.flow.Flow

/**
 * 新闻分析Repository接口
 */
interface NewsAnalysisRepository {
    
    /**
     * 分析新闻内容
     */
    suspend fun analyzeNews(
        newsContent: String,
        options: AnalysisOptions
    ): Result<NewsAnalysis>
    
    /**
     * 获取分析历史
     */
    fun getAnalysisHistory(): Flow<List<NewsAnalysis>>
    
    /**
     * 根据ID获取分析结果
     */
    suspend fun getAnalysisById(id: String): NewsAnalysis?
    
    /**
     * 保存分析结果
     */
    suspend fun saveAnalysis(analysis: NewsAnalysis): Result<Unit>
    
    /**
     * 删除分析结果
     */
    suspend fun deleteAnalysis(id: String): Result<Unit>
    
    /**
     * 获取缓存的分析结果（如果有）
     */
    suspend fun getCachedAnalysis(newsContent: String): NewsAnalysis?
}
