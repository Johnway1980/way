package com.alphadoer.trader.data.remote.api

import com.alphadoer.trader.data.remote.dto.QianfanChatRequest
import com.alphadoer.trader.data.remote.dto.QianfanChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * AI分析服务接口
 * 适配百度千帆 Chat Completions API
 */
interface AIService {

    /**
     * 分析新闻内容
     * 百度千帆API端点：POST /v2/chat/completions
     */
    @POST("v2/chat/completions")
    suspend fun analyzeNews(
        @Body request: QianfanChatRequest
    ): QianfanChatResponse

    /**
     * 分析市场情绪（暂未实现，使用相同端点）
     */
    @POST("v2/chat/completions")
    suspend fun analyzeMarket(
        @Body request: QianfanChatRequest
    ): QianfanChatResponse

    /**
     * 分析股票（暂未实现，使用相同端点）
     */
    @POST("v2/chat/completions")
    suspend fun analyzeStock(
        @Body request: QianfanChatRequest
    ): QianfanChatResponse
}
