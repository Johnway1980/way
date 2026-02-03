package com.alphadoer.trader.presentation.morningreading

/**
 * 早间阅读事件
 */
sealed class MorningReadingEvent {
    data class NewsTextChanged(val text: String) : MorningReadingEvent()
    data class NewsSourceChanged(val source: String) : MorningReadingEvent()
    data class NewsUrlChanged(val url: String) : MorningReadingEvent()
    data class NewsTimeChanged(val time: String) : MorningReadingEvent()
    data class NewsTagsChanged(val tagsCsv: String) : MorningReadingEvent()
    data class AnalysisOptionsChanged(val options: com.alphadoer.trader.domain.model.AnalysisOptions) : MorningReadingEvent()
    object AnalyzeNews : MorningReadingEvent()
    data class SaveAnalysis(val analysis: com.alphadoer.trader.domain.model.NewsAnalysis) : MorningReadingEvent()
    data class ApplyToPlan(val analysisId: String) : MorningReadingEvent()
    // 从分析结果一键写入“强势板块记录”（TOP3板块，自动补齐至≥5股）
    data class LinkSectorsFromAnalysis(val analysisId: String) : MorningReadingEvent()
    data class ViewHistoryDetail(val analysisId: String) : MorningReadingEvent()
    object DismissHistoryDialog : MorningReadingEvent()
    data class DeleteAnalysis(val analysisId: String) : MorningReadingEvent()
    object ClearError : MorningReadingEvent()
    data class UseTemplate(val templateType: NewsTemplate) : MorningReadingEvent()
    // 按日期浏览历史
    data class SetHistoryDateFilter(val date: String?) : MorningReadingEvent()
    data class ViewHistoryByDate(val date: String) : MorningReadingEvent()
}

/**
 * 新闻模板类型
 */
enum class NewsTemplate {
    INTERNATIONAL,  // 国际新闻
    DOMESTIC_POLICY, // 国内政策
    INDUSTRY,       // 行业动态
    COMPANY         // 公司公告
}
