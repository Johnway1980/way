package com.alphadoer.trader.domain.repository

import com.alphadoer.trader.domain.model.process.DailyProcess
import com.alphadoer.trader.domain.model.process.ProcessStep
import com.alphadoer.trader.domain.model.process.StepType
import kotlinx.coroutines.flow.Flow

/**
 * 流程Repository接口
 */
interface ProcessRepository {
    
    /**
     * 获取指定日期的流程状态
     */
    fun getProcessByDate(date: String): Flow<DailyProcess?>
    
    /**
     * 保存流程状态
     */
    suspend fun saveProcess(process: DailyProcess): Result<Unit>
    
    /**
     * 删除指定日期的流程
     */
    suspend fun deleteProcessByDate(date: String): Result<Unit>
    
    /**
     * 获取步骤配置
     */
    suspend fun getStepConfig(stepType: StepType): ProcessStep?
    
    /**
     * 获取所有步骤配置
     */
    suspend fun getAllStepConfigs(): List<ProcessStep>
    
    /**
     * 初始化步骤配置（预填充）
     */
    suspend fun initializeStepConfigs(): Result<Unit>
}
