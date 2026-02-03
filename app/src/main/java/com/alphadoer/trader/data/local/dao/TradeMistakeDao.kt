package com.alphadoer.trader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alphadoer.trader.data.local.entity.TradeMistakeEntity

/**
 * 交易错误DAO
 */
@Dao
interface TradeMistakeDao {
    
    @Query("SELECT * FROM trade_mistakes WHERE id = :id")
    suspend fun getMistakeById(id: String): TradeMistakeEntity?
    
    @Query("SELECT * FROM trade_mistakes WHERE date = :date ORDER BY createdAt DESC")
    suspend fun getMistakesByDate(date: String): List<TradeMistakeEntity>
    
    @Query("SELECT * FROM trade_mistakes WHERE tradeRecordId = :tradeRecordId")
    suspend fun getMistakesByTradeRecord(tradeRecordId: String): List<TradeMistakeEntity>
    
    @Query("SELECT * FROM trade_mistakes ORDER BY createdAt DESC")
    suspend fun getAllMistakes(): List<TradeMistakeEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(mistake: TradeMistakeEntity)
    
    @Query("DELETE FROM trade_mistakes WHERE id = :id")
    suspend fun deleteMistake(id: String)
}
