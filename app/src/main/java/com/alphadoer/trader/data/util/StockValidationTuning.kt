package com.alphadoer.trader.data.util

/**
 * AI分析规则校准的轻量配置（不改动DI架构，直接被验证器使用）。
 */
object StockValidationTuning {
    // 推荐理由最小长度（用于过滤空洞理由）
    @Volatile var minReasonLength: Int = 10

    // 严格领域匹配：当公司/新闻领域为空时是否判定不匹配（默认关闭以兼容旧数据）
    @Volatile var strictNullDomainMismatch: Boolean = false

    // 采样日志：是否启用，以及采样比例（0.0~1.0）
    @Volatile var enableSamplingLog: Boolean = true
    @Volatile var samplingRatio: Double = 0.1

    // 未来：可扩展更多阈值，如领域相关度、置信度调权等
}
