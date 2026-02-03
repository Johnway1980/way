package com.alphadoer.trader.domain.usecase

import com.alphadoer.trader.domain.model.AnalysisOptions
import com.alphadoer.trader.domain.model.NewsAnalysis
import com.alphadoer.trader.domain.repository.NewsAnalysisRepository
import javax.inject.Inject

/**
 * 分析新闻用例
 */
class AnalyzeNewsUseCase @Inject constructor(
    private val newsAnalysisRepository: NewsAnalysisRepository
) {
    suspend operator fun invoke(
        newsContent: String,
        options: AnalysisOptions
    ): Result<NewsAnalysis> {
        // 验证输入
        if (newsContent.isBlank()) {
            return Result.failure(IllegalArgumentException("新闻内容不能为空"))
        }
        
        // 先检查缓存
        val cached = newsAnalysisRepository.getCachedAnalysis(newsContent)
        if (cached != null) {
            return Result.success(cached)
        }
        
        // 调用Repository进行分析
        return newsAnalysisRepository.analyzeNews(newsContent, options)
    }
}
