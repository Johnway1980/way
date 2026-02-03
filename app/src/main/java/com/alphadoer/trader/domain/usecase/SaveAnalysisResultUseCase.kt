package com.alphadoer.trader.domain.usecase

import com.alphadoer.trader.domain.model.NewsAnalysis
import com.alphadoer.trader.domain.repository.NewsAnalysisRepository
import javax.inject.Inject

/**
 * 保存分析结果用例
 */
class SaveAnalysisResultUseCase @Inject constructor(
    private val newsAnalysisRepository: NewsAnalysisRepository
) {
    suspend operator fun invoke(analysis: NewsAnalysis): Result<Unit> {
        return newsAnalysisRepository.saveAnalysis(analysis)
    }
}
