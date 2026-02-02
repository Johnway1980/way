package com.alphadoer.trader.data.local.dao

import androidx.room.*
import com.alphadoer.trader.data.local.entity.StockEntity
import kotlinx.coroutines.flow.Flow

/**
 * 股票信息DAO
 */
@Dao
interface StockDao {
    
    @Query("SELECT * FROM stock ORDER BY name ASC")
    fun getAllStocks(): Flow<List<StockEntity>>
    
    @Query("SELECT * FROM stock WHERE code = :code")
    suspend fun getStockByCode(code: String): StockEntity?
    
    @Query("SELECT * FROM stock WHERE code = :code")
    fun getStockByCodeFlow(code: String): Flow<StockEntity?>
    
    @Query("SELECT * FROM stock WHERE name LIKE :namePattern OR code LIKE :codePattern ORDER BY name ASC")
    suspend fun searchStocks(namePattern: String, codePattern: String): List<StockEntity>
    
    @Query("SELECT * FROM stock WHERE sector = :sector ORDER BY name ASC")
    suspend fun getStocksBySector(sector: String): List<StockEntity>
    
    @Query("SELECT * FROM stock WHERE sector = :sector ORDER BY name ASC")
    fun getStocksBySectorFlow(sector: String): Flow<List<StockEntity>>
    
    @Query("SELECT * FROM stock WHERE industry = :industry ORDER BY name ASC")
    suspend fun getStocksByIndustry(industry: String): List<StockEntity>
    
    @Query("SELECT * FROM stock WHERE isFavorite = 1 ORDER BY name ASC")
    suspend fun getFavoriteStocks(): List<StockEntity>
    
    @Query("SELECT * FROM stock WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteStocksFlow(): Flow<List<StockEntity>>
    
    @Query("UPDATE stock SET isFavorite = :isFavorite, updatedAt = :timestamp WHERE code = :code")
    suspend fun updateFavoriteStatus(code: String, isFavorite: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<StockEntity>)
    
    @Update
    suspend fun updateStock(stock: StockEntity)
    
    @Delete
    suspend fun deleteStock(stock: StockEntity)
    
    @Query("DELETE FROM stock WHERE code = :code")
    suspend fun deleteStockByCode(code: String)
    
    @Query("SELECT COUNT(*) FROM stock")
    suspend fun getStockCount(): Int
}
