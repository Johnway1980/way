package com.alphadoer.trader.domain.model.review

import com.squareup.moshi.JsonClass
import java.util.UUID

/**
 * 交易错误详情
 */
@JsonClass(generateAdapter = true)
data class TradeMistake(
    val id: String = UUID.randomUUID().toString(),
    val tradeRecordId: String,        // 关联的交易记录ID
    val date: String,                 // yyyy-MM-dd
    val mistakeType: MistakeType,
    val category: MistakeCategory,
    val description: String,          // 错误描述
    val rootCause: String,            // 根本原因
    val impactAmount: Double,          // 影响金额（亏损）
    val context: MistakeContext,      // 错误发生的情境
    val improvementMeasures: List<String>, // 改进措施
    val relatedMistakes: List<String>,    // 关联的历史错误ID
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 错误类型
     */
    enum class MistakeType {
        COGNITIVE,        // 认知错误
        EXECUTION,        // 执行错误
        DISCIPLINE,       // 纪律错误
        EMOTIONAL         // 情绪错误
    }
    
    /**
     * 错误分类
     */
    enum class MistakeCategory {
        // 认知错误
        ANALYSIS_ERROR,           // 分析错误
        JUDGMENT_ERROR,           // 判断错误
        OVERCONFIDENCE,           // 过度自信
        
        // 执行错误
        CHASING_HIGH,            // 追高
        PANIC_SELLING,           // 杀跌
        HESITATION,              // 犹豫
        OVERTRADING,             // 过度交易
        
        // 纪律错误
        NO_PLAN,                 // 不按计划
        NO_STOP_LOSS,            // 不止损
        POSITION_OVERRUN,        // 仓位失控
        
        // 情绪错误
        GREED,                   // 贪婪
        FEAR,                    // 恐惧
        REVENGE_TRADING          // 报复交易
    }
    
    /**
     * 错误情境
     */
    @JsonClass(generateAdapter = true)
    data class MistakeContext(
        val marketEnvironment: String, // 市场环境描述
        val emotionState: String,      // 情绪状态
        val timeOfDay: String,         // 交易时段
        val stockCode: String,         // 相关股票
        val notes: String?             // 备注
    )
}
