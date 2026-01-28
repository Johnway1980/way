package com.alphadoer.trader.data.util

import android.util.Log
import com.alphadoer.trader.data.remote.dto.QianfanChatRequest
import com.alphadoer.trader.data.remote.dto.QianfanChatResponse
import com.alphadoer.trader.data.remote.interceptor.HttpException
import com.alphadoer.trader.domain.model.NewsAnalysis
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * 诊断日志工具
 * 用于调试AI新闻分析模块
 */
object DiagnosticsLogger {
    
    private const val TAG = "AI_Diagnostics"
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    
    /**
     * 记录分析请求开始
     */
    fun logAnalysisStart(newsContent: String, useMockData: Boolean) {
        Log.d(TAG, "========== AI分析请求开始 ==========")
        Log.d(TAG, "使用Mock数据: $useMockData")
        Log.d(TAG, "新闻内容长度: ${newsContent.length} 字符")
        Log.d(TAG, "新闻内容摘要: ${newsContent.take(200)}${if (newsContent.length > 200) "..." else ""}")
        Log.d(TAG, "完整内容Hash: ${newsContent.hashCode()}")
    }
    
    /**
     * 记录请求体
     */
    fun logRequest(request: QianfanChatRequest) {
        try {
            val requestAdapter = moshi.adapter(QianfanChatRequest::class.java)
            val requestJson = requestAdapter.toJson(request)
            Log.d(TAG, "========== 请求体 ==========")
            Log.d(TAG, requestJson)
            Log.d(TAG, "消息数量: ${request.messages.size}")
            request.messages.forEachIndexed { index, message ->
                // content 现在是 List<ContentItem>，计算所有文本的总长度
                val contentLength = message.content.sumOf { it.text.length }
                Log.d(TAG, "消息[$index]: role=${message.role}, content项数=${message.content.size}, 总文本长度=$contentLength")
            }
        } catch (e: Exception) {
            Log.e(TAG, "序列化请求体失败: ${e.message}", e)
        }
    }
    
    /**
     * 记录API响应
     */
    fun logApiResponse(response: QianfanChatResponse, statusCode: Int? = null) {
        Log.d(TAG, "========== API响应 ==========")
        Log.d(TAG, "HTTP状态码: ${statusCode ?: "未知"}")
        Log.d(TAG, "错误代码: ${response.errorCode}")
        Log.d(TAG, "错误消息: ${response.errorMsg}")
        
        // 从 choices 或 result 中提取内容
        val content = response.choices?.firstOrNull()?.message?.content ?: response.result
        Log.d(TAG, "结果长度: ${content?.length ?: 0}")
        if (content != null) {
            Log.d(TAG, "结果摘要: ${content.take(500)}")
        }
        
        if (response.usage != null) {
            Log.d(TAG, "Token使用: prompt=${response.usage.promptTokens}, completion=${response.usage.completionTokens}, total=${response.usage.totalTokens}")
        }
        
        Log.d(TAG, "Choices数量: ${response.choices?.size ?: 0}")
        if (response.choices != null && response.choices.isNotEmpty()) {
            Log.d(TAG, "第一个choice的content长度: ${response.choices[0].message?.content?.length ?: 0}")
        }
    }
    
    /**
     * 记录Mock数据生成
     */
    fun logMockDataGeneration(newsContent: String) {
        Log.w(TAG, "========== 使用Mock数据生成 ==========")
        Log.w(TAG, "警告: 当前使用Mock数据，响应将与输入内容无关")
        Log.w(TAG, "新闻内容Hash: ${newsContent.hashCode()}")
    }
    
    /**
     * 记录处理后的分析结果
     */
    fun logAnalysisResult(analysis: NewsAnalysis) {
        Log.d(TAG, "========== 最终分析结果 ==========")
        Log.d(TAG, "ID: ${analysis.id}")
        Log.d(TAG, "摘要: ${analysis.summary}")
        Log.d(TAG, "情绪: ${analysis.sentiment}")
        Log.d(TAG, "置信度: ${analysis.confidence}")
        Log.d(TAG, "关键点数量: ${analysis.keyPoints.size}")
        Log.d(TAG, "受影响板块数量: ${analysis.affectedSectors.size}")
        Log.d(TAG, "推荐股票数量: ${analysis.recommendedStocks.size}")
        Log.d(TAG, "风险警告数量: ${analysis.riskWarnings.size}")
        Log.d(TAG, "数据源: ${analysis.metadata?.get("source") ?: "未知"}")
    }
    
    /**
     * 记录网络错误
     */
    fun logNetworkError(error: Throwable, newsContent: String) {
        Log.e(TAG, "========== 网络错误 ==========")
        Log.e(TAG, "错误类型: ${error.javaClass.simpleName}")
        Log.e(TAG, "错误消息: ${error.message}")
        if (error is HttpException) {
            Log.e(TAG, "HTTP状态码: ${error.code}")
            Log.e(TAG, "错误响应体: ${error.body}")
        }
        Log.e(TAG, "新闻内容Hash: ${newsContent.hashCode()}")
        error.printStackTrace()
    }
    
    /**
     * 记录数据转换
     */
    fun logDataTransformation(step: String, data: Any?) {
        Log.d(TAG, "数据转换步骤: $step")
        Log.d(TAG, "数据类型: ${data?.javaClass?.simpleName ?: "null"}")
    }
    
    /**
     * 记录缓存操作
     */
    fun logCacheOperation(operation: String, key: String, success: Boolean) {
        Log.d(TAG, "缓存操作: $operation")
        Log.d(TAG, "缓存键: $key")
        Log.d(TAG, "操作结果: ${if (success) "成功" else "失败"}")
    }
}
