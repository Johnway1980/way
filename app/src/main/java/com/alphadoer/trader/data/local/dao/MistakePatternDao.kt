package com.alphadoer.trader.data.local.dao

import androidx.room.*
import com.alphadoer.trader.data.local.entity.MistakePatternEntity
import kotlinx.coroutines.flow.Flow

/**
 * 错误模式DAO
 */
@Dao
interface MistakePatternDao {
    
    @Query("SELECT * FROM mistake_pattern ORDER BY createdAt DESC")
    fun getAllPatterns(): Flow<List<MistakePatternEntity>>
    
    @Query("SELECT * FROM mistake_pattern WHERE id = :id")
    suspend fun getPatternById(id: Long): MistakePatternEntity?
    
    @Query("SELECT * FROM mistake_pattern WHERE journalDate = :date ORDER BY createdAt DESC")
    suspend fun getPatternsByDate(date: String): List<MistakePatternEntity>
    
    @Query("SELECT * FROM mistake_pattern WHERE journalDate = :date ORDER BY createdAt DESC")
    fun getPatternsByDateFlow(date: String): Flow<List<MistakePatternEntity>>
    
    @Query("SELECT * FROM mistake_pattern WHERE patternType = :type ORDER BY createdAt DESC")
    suspend fun getPatternsByType(type: String): List<MistakePatternEntity>
    
    @Query("SELECT * FROM mistake_pattern WHERE patternType = :type ORDER BY createdAt DESC")
    fun getPatternsByTypeFlow(type: String): Flow<List<MistakePatternEntity>>
    
    @Query("SELECT * FROM mistake_pattern WHERE severity >= :minSeverity ORDER BY severity DESC, createdAt DESC")
    suspend fun getPatternsByMinSeverity(minSeverity: Int): List<MistakePatternEntity>
    
    @Query("SELECT * FROM mistake_pattern WHERE journalDate BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getPatternsByDateRange(startDate: String, endDate: String): List<MistakePatternEntity>
    
    @Query("SELECT COUNT(*) FROM mistake_pattern WHERE patternType = :type")
    suspend fun getPatternCountByType(type: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: MistakePatternEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatterns(patterns: List<MistakePatternEntity>)
    
    @Update
    suspend fun updatePattern(pattern: MistakePatternEntity)
    
    @Delete
    suspend fun deletePattern(pattern: MistakePatternEntity)
    
    @Query("DELETE FROM mistake_pattern WHERE id = :id")
    suspend fun deletePatternById(id: Long)
    
    @Query("DELETE FROM mistake_pattern WHERE journalDate = :date")
    suspend fun deletePatternsByDate(date: String)
}
