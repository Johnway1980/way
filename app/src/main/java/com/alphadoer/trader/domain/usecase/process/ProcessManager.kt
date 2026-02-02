package com.alphadoer.trader.domain.usecase.process

import com.alphadoer.trader.domain.model.process.DailyProcess
import com.alphadoer.trader.domain.model.process.ProcessStep
import com.alphadoer.trader.domain.model.process.StepState
import kotlinx.coroutines.flow.Flow

/**
 * 流程管理器接口
 */
interface ProcessManager {
    
    /**
     * 获取当日流程状态
     */
    fun getTodayProcess(): Flow<DailyProcess?>
    
    /**
     * 获取指定日期的流程状态
     */
    suspend fun getProcessByDate(date: String): DailyProcess?
    
    /**
     * 初始化当日流程
     */
    suspend fun initializeTodayProcess(): Result<DailyProcess>
    
    /**
     * 开始步骤
     */
    suspend fun startStep(stepId: String): Result<Unit>
    
    /**
     * 完成步骤
     */
    suspend fun completeStep(stepId: String, notes: String? = null, data: Map<String, String>? = null): Result<Unit>
    
    /**
     * 跳过步骤
     */
    suspend fun skipStep(stepId: String, reason: String? = null): Result<Unit>
    
    /**
     * 获取下一步建议
     */
    suspend fun getNextStep(): ProcessStep?
    
    /**
     * 检查步骤是否可用
     */
    suspend fun isStepAvailable(stepId: String): Boolean
    
    /**
     * 重置流程
     */
    suspend fun resetProcess(date: String? = null): Result<Unit>
    
    /**
     * 获取步骤配置
     */
    suspend fun getStepConfig(stepType: com.alphadoer.trader.domain.model.process.StepType): ProcessStep?
    
    /**
     * 获取所有步骤配置
     */
    suspend fun getAllStepConfigs(): List<ProcessStep>
    
    /**
     * 检查时间窗口
     */
    suspend fun checkTimeWindow(stepId: String): Boolean
}
