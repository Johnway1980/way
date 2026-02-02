package com.alphadoer.trader.data.datasource.mock

import kotlin.random.Random

/**
 * 模拟行情数据
 */
object MockMarketData {
    
    /**
     * 获取模拟价格
     */
    fun getMockPrice(stockCode: String, basePrice: Double? = null): Double {
        val base = basePrice ?: (10.0 + Random.nextDouble() * 100.0)
        // 模拟价格波动（±2%）
        val variation = base * (Random.nextDouble() * 0.04 - 0.02)
        return (base + variation).let { 
            if (it < 0.01) 0.01 else it 
        }
    }
    
    /**
     * 获取模拟涨跌幅
     */
    fun getMockChangeRate(): Double {
        return (Random.nextDouble() * 10.0 - 5.0) // -5% 到 +5%
    }
    
    /**
     * 模拟交易执行
     */
    fun simulateTradeExecution(
        stockCode: String,
        operation: String,
        price: Double,
        quantity: Int
    ): Boolean {
        // 模拟交易成功（90%成功率）
        return Random.nextDouble() > 0.1
    }
}
