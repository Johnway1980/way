package com.alphadoer.trader.domain.repository

import com.alphadoer.trader.domain.model.statistics.BacktestConfig
import com.alphadoer.trader.domain.model.statistics.BacktestResult
import com.alphadoer.trader.domain.model.statistics.PeriodPerformance
import com.alphadoer.trader.domain.model.statistics.StatisticalInsight
import com.alphadoer.trader.domain.model.statistics.TradingPattern
import com.alphadoer.trader.domain.model.statistics.TradingPerformance
import kotlinx.coroutines.flow.Flow

/**
 * 统计分析Repository接口
 */
interface StatisticsRepository {
    
    // ========== 绩效统计 ==========
    suspend fun getTradingPerformance(period: String): TradingPerformance?
    suspend fun getPeriodPerformance(periodType: PeriodPerformance.PeriodType, period: String): PeriodPerformance?
    suspend fun getAllPeriodPerformances(periodType: PeriodPerformance.PeriodType): Flow<List<PeriodPerformance>>
    suspend fun calculatePerformanceMetrics(startDate: String, endDate: String): TradingPerformance
    
    // ========== 模式识别 ==========
    suspend fun identifyTradingPatterns(startDate: String, endDate: String): List<TradingPattern>
    suspend fun saveTradingPattern(pattern: TradingPattern): Result<Unit>
    suspend fun getAllTradingPatterns(): Flow<List<TradingPattern>>
    suspend fun validatePattern(patternId: String): TradingPattern.ValidationResult?
    
    // ========== 回测分析 ==========
    suspend fun saveBacktestConfig(config: BacktestConfig): Result<Unit>
    suspend fun getBacktestConfig(id: String): BacktestConfig?
    suspend fun getAllBacktestConfigs(): Flow<List<BacktestConfig>>
    suspend fun runBacktest(config: BacktestConfig): Result<BacktestResult>
    suspend fun getBacktestResult(configId: String): BacktestResult?
    suspend fun getAllBacktestResults(): Flow<List<BacktestResult>>
    
    // ========== 洞察报告 ==========
    suspend fun generateStatisticalInsights(startDate: String, endDate: String): List<StatisticalInsight>
    suspend fun saveStatisticalInsight(insight: StatisticalInsight): Result<Unit>
    suspend fun getAllStatisticalInsights(): Flow<List<StatisticalInsight>>
}
