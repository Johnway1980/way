package com.alphadoer.trader.data.util

import com.alphadoer.trader.domain.model.trading.TradeRecord

/**
 * 基于交易记录生成建议的轻量工具类（无需改变DI结构）
 */
object TradeAdviceGenerator {
    fun generateForTrade(trade: TradeRecord): List<String> {
        val advice = mutableListOf<String>()

        // 止损建议
        if (trade.isBuy() && trade.stopLoss == null) {
            advice += "建议设置止损位，避免扩大亏损"
        }

        // 盈亏建议
        trade.profitLoss?.let { pl ->
            when {
                pl < 0 -> advice += "本次出现亏损，复盘入场逻辑与时机"
                pl > 0 -> advice += "有盈利，考虑分批止盈或提高纪律执行"
            }
        }

        // 仓位建议
        if (trade.quantity > 0 && trade.quantity % 100 != 0) {
            advice += "仓位非标准手数，注意滑点与成交效率"
        }

        // 计划一致性建议
        if (trade.notes?.contains("临时") == true) {
            advice += "临时决策偏多，建议回归计划交易"
        }

        return advice
    }
}
