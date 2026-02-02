package com.alphadoer.trader.data.repository

import com.alphadoer.trader.data.local.dao.StockDao
import com.alphadoer.trader.data.local.entity.StockEntity
import com.alphadoer.trader.domain.model.RecommendedStock
import com.alphadoer.trader.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 股票信息Repository实现
 */
class StockRepositoryImpl @Inject constructor(
    private val stockDao: StockDao
) : StockRepository {
    
    override suspend fun getStockByCode(code: String): RecommendedStock? {
        return try {
            val entity = stockDao.getStockByCode(code)
            entity?.toDomainModel()
        } catch (e: Exception) {
            android.util.Log.e("StockRepositoryImpl", "获取股票信息失败: ${e.message}", e)
            null
        }
    }
    
    override suspend fun searchStocks(query: String): List<RecommendedStock> {
        return try {
            val entities = stockDao.searchStocks(
                namePattern = "%$query%",
                codePattern = "%$query%"
            )
            entities.mapNotNull { entity ->
                try {
                    entity.toDomainModel()
                } catch (e: Exception) {
                    android.util.Log.e("StockRepositoryImpl", "转换股票实体失败: ${e.message}", e)
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("StockRepositoryImpl", "搜索股票失败: ${e.message}", e)
            emptyList()
        }
    }
    
    override fun getFavoriteStocks(): Flow<List<RecommendedStock>> {
        return stockDao.getFavoriteStocksFlow()
            .map { entities -> 
                entities.mapNotNull { entity ->
                    try {
                        entity.toDomainModel()
                    } catch (e: Exception) {
                        android.util.Log.e("StockRepositoryImpl", "转换股票实体失败: ${e.message}", e)
                        null
                    }
                }
            }
    }
    
    override suspend fun addToFavorites(stockCode: String): Result<Unit> {
        return try {
            // 先检查股票是否存在
            val existingStock = stockDao.getStockByCode(stockCode)
            if (existingStock != null) {
                // 如果存在，更新收藏状态
                stockDao.updateFavoriteStatus(stockCode, true)
            } else {
                // 如果不存在，创建新股票
                // 从股票代码推断市场（6位数字代码：6开头=上海，0/3开头=深圳，8开头=北京）
                val market = when {
                    stockCode.startsWith("6") -> "SH"
                    stockCode.startsWith("0") || stockCode.startsWith("3") -> "SZ"
                    stockCode.startsWith("8") -> "BJ"
                    else -> "SH" // 默认上海
                }
                val newStock = StockEntity.create(
                    code = stockCode,
                    name = stockCode, // 临时名称，后续可以通过股票代码查询API获取
                    market = market,
                    isFavorite = true
                )
                stockDao.insertStock(newStock)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun removeFromFavorites(stockCode: String): Result<Unit> {
        return try {
            stockDao.updateFavoriteStatus(stockCode, false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun saveStock(stock: RecommendedStock): Result<Unit> {
        return try {
            val entity = StockEntity.create(
                code = stock.stockCode,
                name = stock.stockName,
                market = stock.market,
                isFavorite = true, // 保存时自动添加到收藏
                notes = stock.reason
            )
            stockDao.insertStock(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 数据转换 ==========
    private fun StockEntity.toDomainModel(): RecommendedStock {
        return RecommendedStock(
            stockCode = code,
            stockName = name,
            market = market,
            recommendation = RecommendedStock.RecommendationType.WATCH,
            reason = notes ?: "",
            confidence = 0.5,
            riskLevel = RecommendedStock.RiskLevel.MEDIUM
        )
    }
}
