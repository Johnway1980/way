package com.alphadoer.trader.data.local.dao

import androidx.room.*
import com.alphadoer.trader.data.local.entity.SectorEntity
import kotlinx.coroutines.flow.Flow

/**
 * 板块信息DAO
 */
@Dao
interface SectorDao {
    
    @Query("SELECT * FROM sector ORDER BY name ASC")
    fun getAllSectors(): Flow<List<SectorEntity>>
    
    @Query("SELECT * FROM sector WHERE code = :code")
    suspend fun getSectorByCode(code: String): SectorEntity?
    
    @Query("SELECT * FROM sector WHERE code = :code")
    fun getSectorByCodeFlow(code: String): Flow<SectorEntity?>
    
    @Query("SELECT * FROM sector WHERE type = :type ORDER BY name ASC")
    suspend fun getSectorsByType(type: String): List<SectorEntity>
    
    @Query("SELECT * FROM sector WHERE type = :type ORDER BY name ASC")
    fun getSectorsByTypeFlow(type: String): Flow<List<SectorEntity>>
    
    @Query("SELECT * FROM sector WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getActiveSectors(): List<SectorEntity>
    
    @Query("SELECT * FROM sector WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveSectorsFlow(): Flow<List<SectorEntity>>
    
    @Query("SELECT * FROM sector WHERE name LIKE :namePattern ORDER BY name ASC")
    suspend fun searchSectors(namePattern: String): List<SectorEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSector(sector: SectorEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSectors(sectors: List<SectorEntity>)
    
    @Update
    suspend fun updateSector(sector: SectorEntity)
    
    @Query("UPDATE sector SET isActive = :isActive, updatedAt = :timestamp WHERE code = :code")
    suspend fun updateActiveStatus(code: String, isActive: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteSector(sector: SectorEntity)
    
    @Query("DELETE FROM sector WHERE code = :code")
    suspend fun deleteSectorByCode(code: String)
    
    @Query("SELECT COUNT(*) FROM sector")
    suspend fun getSectorCount(): Int
}
