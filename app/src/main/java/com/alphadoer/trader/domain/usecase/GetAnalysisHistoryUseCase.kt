package com.alphadoer.trader.domain.usecase

import com.alphadoer.trader.domain.model.NewsAnalysis
import com.alphadoer.trader.domain.repository.NewsAnalysisRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取分析历史用例
 */
class GetAnalysisHistoryUseCase @Inject constructor(
    private val newsAnalysisRepository: NewsAnalysisRepository
) {
    operator fun invoke(): Flow<List<NewsAnalysis>> {
        return newsAnalysisRepository.getAnalysisHistory()
    }
}
