package com.alphadoer.trader.domain.usecase.process

import com.alphadoer.trader.domain.model.process.DailyProcess
import com.alphadoer.trader.domain.model.process.ProcessStep
import com.alphadoer.trader.domain.model.process.StepStatus
import com.alphadoer.trader.domain.model.process.StepState
import com.alphadoer.trader.domain.model.process.StepType
import com.alphadoer.trader.domain.repository.ProcessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * 流程管理器实现
 */
class ProcessManagerImpl @Inject constructor(
    private val processRepository: ProcessRepository
) : ProcessManager {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    override fun getTodayProcess(): Flow<DailyProcess?> {
        val today = dateFormat.format(Date())
        return processRepository.getProcessByDate(today)
    }
    
    override suspend fun getProcessByDate(date: String): DailyProcess? {
        return processRepository.getProcessByDate(date).firstOrNull()
    }
    
    override suspend fun initializeTodayProcess(): Result<DailyProcess> {
        return try {
            val today = dateFormat.format(Date())
            val existing = getProcessByDate(today)
            
            if (existing != null) {
                Result.success(existing)
            } else {
                // 创建新的流程
                val steps = getAllStepConfigs()
                val stepStates = steps.associate { step ->
                    step.id to StepState(
                        stepId = step.id,
                        status = StepStatus.NOT_STARTED
                    )
                }
                
                val process = DailyProcess(
                    id = UUID.randomUUID().toString(),
                    date = today,
                    steps = steps,
                    stepStates = stepStates,
                    overallProgress = 0.0
                )
                
                processRepository.saveProcess(process)
                Result.success(process)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun startStep(stepId: String): Result<Unit> {
        return try {
            val today = dateFormat.format(Date())
            val process = getProcessByDate(today) ?: return Result.failure(
                IllegalStateException("当日流程未初始化")
            )
            
            if (!isStepAvailable(stepId)) {
                return Result.failure(IllegalStateException("步骤不可用"))
            }
            
            val currentState = process.stepStates[stepId] ?: StepState(
                stepId = stepId,
                status = StepStatus.NOT_STARTED
            )
            
            val updatedState = currentState.copy(
                status = StepStatus.IN_PROGRESS,
                startedAt = System.currentTimeMillis()
            )
            
            val updatedStates = process.stepStates.toMutableMap()
            updatedStates[stepId] = updatedState
            
            val updatedProcess = process.copy(
                stepStates = updatedStates,
                updatedAt = System.currentTimeMillis()
            )
            
            processRepository.saveProcess(updatedProcess)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun completeStep(
        stepId: String,
        notes: String?,
        data: Map<String, String>?
    ): Result<Unit> {
        return try {
            val today = dateFormat.format(Date())
            val process = getProcessByDate(today) ?: return Result.failure(
                IllegalStateException("当日流程未初始化")
            )
            
            val currentState = process.stepStates[stepId] ?: return Result.failure(
                IllegalStateException("步骤状态不存在")
            )
            
            val startedAt = currentState.startedAt ?: System.currentTimeMillis()
            val completedAt = System.currentTimeMillis()
            val actualDuration = (completedAt - startedAt) / (1000 * 60) // 转换为分钟
            
            val updatedState = currentState.copy(
                status = StepStatus.COMPLETED,
                completedAt = completedAt,
                actualDuration = actualDuration,
                notes = notes,
                data = data
            )
            
            val updatedStates = process.stepStates.toMutableMap()
            updatedStates[stepId] = updatedState
            
            val updatedProcess = process.copy(
                stepStates = updatedStates,
                overallProgress = process.calculateProgress(),
                updatedAt = System.currentTimeMillis()
            )
            
            processRepository.saveProcess(updatedProcess)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun skipStep(stepId: String, reason: String?): Result<Unit> {
        return try {
            val today = dateFormat.format(Date())
            val process = getProcessByDate(today) ?: return Result.failure(
                IllegalStateException("当日流程未初始化")
            )
            
            val currentState = process.stepStates[stepId] ?: StepState(
                stepId = stepId,
                status = StepStatus.NOT_STARTED
            )
            
            val updatedState = currentState.copy(
                status = StepStatus.SKIPPED,
                skippedAt = System.currentTimeMillis(),
                notes = reason
            )
            
            val updatedStates = process.stepStates.toMutableMap()
            updatedStates[stepId] = updatedState
            
            val updatedProcess = process.copy(
                stepStates = updatedStates,
                overallProgress = process.calculateProgress(),
                updatedAt = System.currentTimeMillis()
            )
            
            processRepository.saveProcess(updatedProcess)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getNextStep(): ProcessStep? {
        val today = dateFormat.format(Date())
        val process = getProcessByDate(today) ?: return null
        return process.getNextAvailableStep()
    }
    
    override suspend fun isStepAvailable(stepId: String): Boolean {
        val today = dateFormat.format(Date())
        val process = getProcessByDate(today) ?: return false
        
        val step = process.steps.find { it.id == stepId } ?: return false
        val state = process.stepStates[stepId]
        
        // 检查状态
        if (state?.status == StepStatus.COMPLETED || state?.status == StepStatus.SKIPPED) {
            return false
        }
        
        // 检查依赖
        val dependenciesMet = step.dependencies.all { depId ->
            process.stepStates[depId]?.status == StepStatus.COMPLETED
        }
        
        if (!dependenciesMet) {
            return false
        }
        
        // 检查时间窗口
        return checkTimeWindow(stepId)
    }
    
    override suspend fun resetProcess(date: String?): Result<Unit> {
        return try {
            val targetDate = date ?: dateFormat.format(Date())
            processRepository.deleteProcessByDate(targetDate)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getStepConfig(stepType: StepType): ProcessStep? {
        return processRepository.getStepConfig(stepType)
    }
    
    override suspend fun getAllStepConfigs(): List<ProcessStep> {
        return processRepository.getAllStepConfigs()
    }
    
    override suspend fun checkTimeWindow(stepId: String): Boolean {
        val today = dateFormat.format(Date())
        val process = getProcessByDate(today) ?: return true
        
        val step = process.steps.find { it.id == stepId } ?: return true
        val timeWindow = step.timeWindow ?: return true
        
        val now = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        try {
            val todayDate = dateFormat.parse(today) ?: return true
            val startTime = timeFormat.parse(timeWindow.startTime) ?: return true
            val endTime = timeFormat.parse(timeWindow.endTime) ?: return true
            
            val startMillis = todayDate.time + startTime.time
            val endMillis = todayDate.time + endTime.time
            
            return now in startMillis..endMillis
        } catch (e: Exception) {
            return true // 解析失败时默认允许
        }
    }
}
