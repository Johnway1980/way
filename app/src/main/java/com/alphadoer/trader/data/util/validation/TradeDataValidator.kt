package com.alphadoer.trader.data.util.validation

import com.alphadoer.trader.domain.model.trading.TradeOperation
import com.alphadoer.trader.domain.model.trading.TradeRecord

/**
 * 交易数据验证器
 */
object TradeDataValidator {
    
    /**
     * 验证股票代码格式
     */
    fun validateStockCode(code: String): ValidationResult {
        if (code.isBlank()) {
            return ValidationResult.Error("股票代码不能为空")
        }
        
        // 中国A股代码格式：6位数字
        val pattern = "^[0-9]{6}$".toRegex()
        if (!pattern.matches(code)) {
            return ValidationResult.Error("股票代码必须是6位数字")
        }
        
        return ValidationResult.Success
    }
    
    /**
     * 验证价格
     */
    fun validatePrice(price: Double): ValidationResult {
        if (price <= 0) {
            return ValidationResult.Error("价格必须大于0")
        }
        if (price > 10000) {
            return ValidationResult.Error("价格超出合理范围")
        }
        return ValidationResult.Success
    }
    
    /**
     * 验证数量
     */
    fun validateQuantity(quantity: Int): ValidationResult {
        if (quantity <= 0) {
            return ValidationResult.Error("数量必须大于0")
        }
        if (quantity % 100 != 0) {
            return ValidationResult.Error("A股交易数量必须是100的整数倍")
        }
        if (quantity > 1000000) {
            return ValidationResult.Error("数量超出合理范围")
        }
        return ValidationResult.Success
    }
    
    /**
     * 验证交易记录
     */
    fun validateTradeRecord(record: TradeRecord): ValidationResult {
        validateStockCode(record.stockCode).let {
            if (it is ValidationResult.Error) return it
        }
        
        validatePrice(record.price).let {
            if (it is ValidationResult.Error) return it
        }
        
        validateQuantity(record.quantity).let {
            if (it is ValidationResult.Error) return it
        }
        
        if (record.stockName.isBlank()) {
            return ValidationResult.Error("股票名称不能为空")
        }
        
        return ValidationResult.Success
    }
    
    /**
     * 验证业务规则
     */
    fun validateBusinessRules(
        record: TradeRecord,
        existingTrades: List<TradeRecord>
    ): ValidationResult {
        if (record.operation == TradeOperation.SELL) {
            // 检查是否有足够的持仓
            val buyTrades = existingTrades.filter { 
                it.stockCode == record.stockCode && it.operation == TradeOperation.BUY 
            }
            val sellTrades = existingTrades.filter { 
                it.stockCode == record.stockCode && it.operation == TradeOperation.SELL 
            }
            
            val totalBuyQuantity = buyTrades.sumOf { it.quantity }
            val totalSellQuantity = sellTrades.sumOf { it.quantity }
            val availableQuantity = totalBuyQuantity - totalSellQuantity
            
            if (record.quantity > availableQuantity) {
                return ValidationResult.Error("卖出数量超过可用持仓")
            }
        }
        
        return ValidationResult.Success
    }
    
    /**
     * 验证结果
     */
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
        
        fun isSuccess(): Boolean = this is Success
        fun isError(): Boolean = this is Error
    }
}
