package com.alphadoer.trader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alphadoer.trader.data.local.entity.ProcessStateEntity
import kotlinx.coroutines.flow.Flow

/**
 * 流程状态DAO
 */
@Dao
interface ProcessStateDao {
    
    @Query("SELECT * FROM process_state WHERE date = :date")
    fun getProcessByDate(date: String): Flow<ProcessStateEntity?>
    
    @Query("SELECT * FROM process_state WHERE date = :date")
    suspend fun getProcessByDateSync(date: String): ProcessStateEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcess(process: ProcessStateEntity)
    
    @Update
    suspend fun updateProcess(process: ProcessStateEntity)
    
    @Query("DELETE FROM process_state WHERE date = :date")
    suspend fun deleteProcessByDate(date: String)
    
    @Query("SELECT * FROM process_state ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentProcesses(limit: Int): List<ProcessStateEntity>
}
