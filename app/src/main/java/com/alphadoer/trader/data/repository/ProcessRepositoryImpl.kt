package com.alphadoer.trader.data.repository

import com.alphadoer.trader.data.local.dao.ProcessStateDao
import com.alphadoer.trader.data.local.dao.StepConfigDao
import com.alphadoer.trader.data.local.entity.ProcessStateEntity
import com.alphadoer.trader.data.local.entity.StepConfigEntity
import com.alphadoer.trader.domain.model.process.DailyProcess
import com.alphadoer.trader.domain.model.process.ProcessStep
import com.alphadoer.trader.domain.model.process.StepState
import com.alphadoer.trader.domain.model.process.StepStatus
import com.alphadoer.trader.domain.model.process.StepType
import com.alphadoer.trader.domain.repository.ProcessRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 流程Repository实现
 */
class ProcessRepositoryImpl @Inject constructor(
    private val processStateDao: ProcessStateDao,
    private val stepConfigDao: StepConfigDao
) : ProcessRepository {
    
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    
    override fun getProcessByDate(date: String): Flow<DailyProcess?> {
        return flow {
            processStateDao.getProcessByDate(date).collect { entity ->
                if (entity != null) {
                    val steps = stepConfigDao.getAllStepConfigs().map { config -> config.toDomainModel() }
                    emit(entity.toDomainModel(steps))
                } else {
                    emit(null)
                }
            }
        }
    }
    
    override suspend fun saveProcess(process: DailyProcess): Result<Unit> {
        return try {
            val entity = process.toEntity()
            processStateDao.insertProcess(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteProcessByDate(date: String): Result<Unit> {
        return try {
            processStateDao.deleteProcessByDate(date)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getStepConfig(stepType: StepType): ProcessStep? {
        val entity = stepConfigDao.getStepConfigByType(stepType.name)
        return entity?.toDomainModel()
    }
    
    override suspend fun getAllStepConfigs(): List<ProcessStep> {
        val entities = stepConfigDao.getAllStepConfigs()
        return entities.map { it.toDomainModel() }
    }
    
    override suspend fun initializeStepConfigs(): Result<Unit> {
        return try {
            val existing = stepConfigDao.getAllStepConfigs()
            if (existing.isEmpty()) {
                val defaultConfigs = createDefaultStepConfigs()
                stepConfigDao.insertStepConfigs(defaultConfigs)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 数据转换 ==========
    private suspend fun ProcessStateEntity.toDomainModel(steps: List<ProcessStep>): DailyProcess {
        val stepStatesType = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            StepState::class.java
        )
        val stepStatesAdapter = moshi.adapter<Map<String, StepState>>(stepStatesType)
        val stepStates = stepStatesAdapter.fromJson(stepStatesJson) ?: emptyMap()
        
        return DailyProcess(
            id = id,
            date = date,
            steps = steps,
            stepStates = stepStates,
            overallProgress = overallProgress,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    private fun DailyProcess.toEntity(): ProcessStateEntity {
        val stepStatesType = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            StepState::class.java
        )
        val stepStatesAdapter = moshi.adapter<Map<String, StepState>>(stepStatesType)
        val stepStatesJson = stepStatesAdapter.toJson(stepStates)
        
        return ProcessStateEntity(
            id = id,
            date = date,
            stepStatesJson = stepStatesJson,
            overallProgress = overallProgress,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    private fun StepConfigEntity.toDomainModel(): ProcessStep {
        val timeWindow = if (timeWindowStart != null && timeWindowEnd != null) {
            ProcessStep.TimeWindow(
                startTime = timeWindowStart,
                endTime = timeWindowEnd,
                timezone = "Asia/Shanghai"
            )
        } else {
            null
        }
        
        return ProcessStep(
            id = stepId,
            type = StepType.values().find { it.name == stepType } ?: StepType.MORNING_READING,
            name = name,
            description = description,
            icon = icon,
            estimatedDuration = estimatedDuration,
            dependencies = dependencies,
            timeWindow = timeWindow,
            route = route,
            required = required,
            order = order
        )
    }
    
    private fun createDefaultStepConfigs(): List<StepConfigEntity> {
        return listOf(
            StepConfigEntity(
                stepId = "step_1",
                stepType = StepType.MORNING_READING.name,
                name = "早间信息阅读",
                description = "阅读并分析早间新闻，了解市场动态",
                icon = null,
                estimatedDuration = 30,
                dependencies = emptyList(),
                timeWindowStart = "08:00",
                timeWindowEnd = "09:30",
                route = "morning_reading",
                required = true,
                order = 1
            ),
            StepConfigEntity(
                stepId = "step_2",
                stepType = StepType.PRE_MARKET_PLAN.name,
                name = "盘前计划",
                description = "制定今日交易计划",
                icon = null,
                estimatedDuration = 20,
                dependencies = listOf("step_1"),
                timeWindowStart = "09:00",
                timeWindowEnd = "09:30",
                route = "pre_market_plan",
                required = true,
                order = 2
            ),
            StepConfigEntity(
                stepId = "step_3",
                stepType = StepType.AUCTION_OBSERVATION.name,
                name = "集合竞价观察",
                description = "观察集合竞价情况",
                icon = null,
                estimatedDuration = 10,
                dependencies = listOf("step_2"),
                timeWindowStart = "09:15",
                timeWindowEnd = "09:30",
                route = "auction_observation",
                required = true,
                order = 3
            ),
            StepConfigEntity(
                stepId = "step_4",
                stepType = StepType.TRADING.name,
                name = "盘中交易",
                description = "执行交易操作",
                icon = null,
                estimatedDuration = 240,
                dependencies = listOf("step_3"),
                timeWindowStart = "09:30",
                timeWindowEnd = "15:00",
                route = "trading",
                required = true,
                order = 4
            ),
            StepConfigEntity(
                stepId = "step_5",
                stepType = StepType.POST_TRADING_REVIEW.name,
                name = "盘后复盘",
                description = "复盘今日交易情况",
                icon = null,
                estimatedDuration = 30,
                dependencies = listOf("step_4"),
                timeWindowStart = "15:00",
                timeWindowEnd = "18:00",
                route = "post_trading_review",
                required = true,
                order = 5
            ),
            StepConfigEntity(
                stepId = "step_6",
                stepType = StepType.MISTAKE_ANALYSIS.name,
                name = "错误分析",
                description = "分析交易中的错误",
                icon = null,
                estimatedDuration = 20,
                dependencies = listOf("step_5"),
                timeWindowStart = null,
                timeWindowEnd = null,
                route = "mistake_analysis",
                required = true,
                order = 6
            ),
            StepConfigEntity(
                stepId = "step_7",
                stepType = StepType.IMPROVEMENT_PLAN.name,
                name = "改进计划",
                description = "制定改进计划",
                icon = null,
                estimatedDuration = 20,
                dependencies = listOf("step_6"),
                timeWindowStart = null,
                timeWindowEnd = null,
                route = "improvement_plan",
                required = true,
                order = 7
            ),
            StepConfigEntity(
                stepId = "step_8",
                stepType = StepType.NEXT_DAY_PREP.name,
                name = "次日准备",
                description = "为次日交易做准备",
                icon = null,
                estimatedDuration = 15,
                dependencies = listOf("step_7"),
                timeWindowStart = null,
                timeWindowEnd = null,
                route = "next_day_prep",
                required = true,
                order = 8
            )
        )
    }
}
