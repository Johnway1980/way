package com.alphadoer.trader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alphadoer.trader.data.local.entity.StepConfigEntity

/**
 * 步骤配置DAO
 */
@Dao
interface StepConfigDao {
    
    @Query("SELECT * FROM step_config WHERE step_id = :stepId")
    suspend fun getStepConfigById(stepId: String): StepConfigEntity?
    
    @Query("SELECT * FROM step_config WHERE step_type = :stepType")
    suspend fun getStepConfigByType(stepType: String): StepConfigEntity?
    
    @Query("SELECT * FROM step_config ORDER BY `order` ASC")
    suspend fun getAllStepConfigs(): List<StepConfigEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStepConfig(config: StepConfigEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStepConfigs(configs: List<StepConfigEntity>)
    
    @Query("DELETE FROM step_config")
    suspend fun deleteAllStepConfigs()
}
