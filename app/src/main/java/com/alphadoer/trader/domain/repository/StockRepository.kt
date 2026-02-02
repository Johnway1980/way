package com.alphadoer.trader.domain.repository

import com.alphadoer.trader.domain.model.RecommendedStock
import kotlinx.coroutines.flow.Flow

/**
 * 股票信息Repository接口
 */
interface StockRepository {
    
    /**
     * 根据代码获取股票信息
     */
    suspend fun getStockByCode(code: String): RecommendedStock?
    
    /**
     * 搜索股票
     */
    suspend fun searchStocks(query: String): List<RecommendedStock>
    
    /**
     * 获取收藏的股票列表
     */
    fun getFavoriteStocks(): Flow<List<RecommendedStock>>
    
    /**
     * 添加股票到收藏
     */
    suspend fun addToFavorites(stockCode: String): Result<Unit>
    
    /**
     * 从收藏中移除
     */
    suspend fun removeFromFavorites(stockCode: String): Result<Unit>
    
    /**
     * 保存股票信息（如果不存在则创建，存在则更新）
     */
    suspend fun saveStock(stock: RecommendedStock): Result<Unit>
}
