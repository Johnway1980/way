package com.alphadoer.trader.data.util.validation

import com.alphadoer.trader.domain.model.trading.TradeRecord

/**
 * 交易数据清理器
 */
object TradeDataCleaner {
    
    /**
     * 清理股票代码
     */
    fun cleanStockCode(code: String): String {
        return code.trim().uppercase()
    }
    
    /**
     * 清理股票名称
     */
    fun cleanStockName(name: String): String {
        return name.trim()
    }
    
    /**
     * 清理价格
     */
    fun cleanPrice(price: String): Double? {
        return price.trim().toDoubleOrNull()
    }
    
    /**
     * 清理数量
     */
    fun cleanQuantity(quantity: String): Int? {
        return quantity.trim().toIntOrNull()
    }
    
    /**
     * 规范化交易记录
     */
    fun normalizeTradeRecord(record: TradeRecord): TradeRecord {
        return record.copy(
            stockCode = cleanStockCode(record.stockCode),
            stockName = cleanStockName(record.stockName),
            reason = record.reason?.trim()?.takeIf { it.isNotBlank() },
            notes = record.notes?.trim()?.takeIf { it.isNotBlank() }
        )
    }
}
