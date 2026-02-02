package com.alphadoer.trader.data.local.dao

import androidx.room.*
import com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity
import kotlinx.coroutines.flow.Flow

/**
 * AI分析缓存DAO
 */
@Dao
interface AIAnalysisCacheDao {
    
    @Query("SELECT * FROM ai_analysis_cache WHERE cacheKey = :key")
    suspend fun getCacheByKey(key: String): AIAnalysisCacheEntity?
    
    @Query("SELECT * FROM ai_analysis_cache WHERE cacheKey = :key")
    fun getCacheByKeyFlow(key: String): Flow<AIAnalysisCacheEntity?>
    
    @Query("SELECT * FROM ai_analysis_cache WHERE analysisType = :type ORDER BY createdAt DESC")
    suspend fun getCachesByType(type: String): List<AIAnalysisCacheEntity>
    
    @Query("SELECT * FROM ai_analysis_cache WHERE analysisType = :type ORDER BY createdAt DESC")
    fun getCachesByTypeFlow(type: String): Flow<List<AIAnalysisCacheEntity>>
    
    @Query("SELECT * FROM ai_analysis_cache WHERE expiresAt IS NULL OR expiresAt > :currentTime ORDER BY createdAt DESC")
    suspend fun getValidCaches(currentTime: Long = System.currentTimeMillis()): List<AIAnalysisCacheEntity>
    
    @Query("DELETE FROM ai_analysis_cache WHERE expiresAt IS NOT NULL AND expiresAt <= :currentTime")
    suspend fun deleteExpiredCaches(currentTime: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM ai_analysis_cache WHERE cacheKey = :key")
    suspend fun deleteCacheByKey(key: String)
    
    @Query("DELETE FROM ai_analysis_cache WHERE analysisType = :type")
    suspend fun deleteCachesByType(type: String)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: AIAnalysisCacheEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaches(caches: List<AIAnalysisCacheEntity>)
    
    @Update
    suspend fun updateCache(cache: AIAnalysisCacheEntity)
    
    @Delete
    suspend fun deleteCache(cache: AIAnalysisCacheEntity)
    
    @Query("SELECT COUNT(*) FROM ai_analysis_cache")
    suspend fun getCacheCount(): Int
}
