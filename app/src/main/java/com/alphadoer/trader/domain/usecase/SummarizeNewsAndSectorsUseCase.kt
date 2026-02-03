package com.alphadoer.trader.domain.usecase

import android.util.Log
import com.alphadoer.trader.data.remote.api.AIService
import com.alphadoer.trader.data.remote.dto.ChatMessage
import com.alphadoer.trader.data.remote.dto.ContentItem
import com.alphadoer.trader.data.remote.dto.QianfanChatRequest
import com.alphadoer.trader.domain.model.NewsAnalysis
import com.alphadoer.trader.domain.model.SectorSelectionRecord
import com.alphadoer.trader.domain.repository.NewsAnalysisRepository
import com.alphadoer.trader.domain.repository.SectorSelectionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 汇总当天新闻与板块记录，并生成AI总结与建议
 */
class SummarizeNewsAndSectorsUseCase @Inject constructor(
    private val aiService: AIService,
    private val newsAnalysisRepository: NewsAnalysisRepository,
    private val sectorSelectionRepository: SectorSelectionRepository
) {
    suspend operator fun invoke(date: String): Result<String> = try {
        // 获取当天新闻分析与板块记录
        val analyses = newsAnalysisRepository.getAnalysisHistory().first()
            .filter { isSameDate(it.createdAt, date) }
        val selections = sectorSelectionRepository.getSelectionsByDate(date).first()

        val systemPrompt = buildString {
            appendLine("你是A股盘后总结助手，请基于提供的当天新闻分析和用户记录的强势板块，生成简洁的盘后总结与可执行建议。")
            appendLine("输出结构：")
            appendLine("- 今日主线板块与逻辑")
            appendLine("- 重点跟踪股票与理由（最多10条）")
            appendLine("- 风险提示与规避建议")
            appendLine("- 明日计划要点（3-5条）")
        }

        val userPrompt = buildUserPrompt(date, analyses, selections)

        val request = QianfanChatRequest(
            model = "ernie-4.5-turbo-vl-latest",
            messages = listOf(
                ChatMessage(role = "system", content = listOf(ContentItem(type = "text", text = systemPrompt))),
                ChatMessage(role = "user", content = listOf(ContentItem(type = "text", text = userPrompt)))
            ),
            stream = false,
            temperature = 0.6,
            topP = 0.8,
            penaltyScore = 1.0
        )

        val response = aiService.analyzeMarket(request)
        val text = response.choices?.firstOrNull()?.message?.content ?: response.result ?: ""
        Result.success(text.trim())
    } catch (e: Exception) {
        Log.e("SummarizeUseCase", "生成总结失败: ${'$'}{e.message}", e)
        Result.failure(e)
    }

    private fun buildUserPrompt(
        date: String,
        analyses: List<NewsAnalysis>,
        selections: List<SectorSelectionRecord>
    ): String {
        return buildString {
            appendLine("日期：${'$'}date")
            appendLine()
            appendLine("[新闻分析摘要]")
            analyses.take(5).forEach { a ->
                appendLine("- 摘要：${'$'}{a.summary}")
                val topSectors = a.affectedSectors.take(2).joinToString { it.sectorName }
                appendLine("  影响板块：${'$'}topSectors")
                val perSectorStocks = a.recommendedStocks.groupBy { it.sectorName ?: "未标明" }
                perSectorStocks.entries.take(2).forEach { (sector, stocks) ->
                    appendLine("  [${'$'}sector] 推荐：${'$'}{stocks.take(3).joinToString { it.stockName }}")
                }
            }
            appendLine()
            appendLine("[用户记录的强势板块]")
            selections.forEach { s ->
                appendLine("- ${'$'}{s.sectorName}（${'$'}{s.sectorCode}）：${'$'}{s.stockCodes.joinToString()}")
            }
        }
    }

    private fun isSameDate(ts: Long, date: String): Boolean {
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return fmt.format(java.util.Date(ts)) == date
    }
}
