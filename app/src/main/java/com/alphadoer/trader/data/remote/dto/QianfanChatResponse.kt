package com.alphadoer.trader.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 百度千帆Chat Completions API响应格式
 * 根据实际API返回格式：包含 choices 数组，内容在 choices[0].message.content 中
 */
@JsonClass(generateAdapter = true)
data class QianfanChatResponse(
    @Json(name = "id")
    val id: String? = null,
    
    @Json(name = "object")
    val objectType: String? = null,
    
    @Json(name = "created")
    val created: Long? = null,
    
    @Json(name = "model")
    val model: String? = null,
    
    /**
     * 选择列表，包含AI的回复
     * 实际内容在 choices[0].message.content 中
     */
    @Json(name = "choices")
    val choices: List<ChoiceItem>? = null,
    
    /**
     * 兼容旧格式：直接返回 result 字段（如果API返回）
     */
    @Json(name = "result")
    val result: String? = null,
    
    @Json(name = "is_end")
    val isEnd: Boolean? = null,
    
    @Json(name = "is_truncated")
    val isTruncated: Boolean? = null,
    
    @Json(name = "need_clear_history")
    val needClearHistory: Boolean? = null,
    
    @Json(name = "usage")
    val usage: UsageInfo? = null,
    
    @Json(name = "error_code")
    val errorCode: Int? = null,
    
    @Json(name = "error_msg")
    val errorMsg: String? = null
)

/**
 * 选择项，包含AI的消息回复
 */
@JsonClass(generateAdapter = true)
data class ChoiceItem(
    @Json(name = "index")
    val index: Int? = null,
    
    @Json(name = "message")
    val message: ChoiceMessage? = null,
    
    @Json(name = "finish_reason")
    val finishReason: String? = null,
    
    @Json(name = "flag")
    val flag: Int? = null
)

/**
 * 选择消息，包含角色和内容
 */
@JsonClass(generateAdapter = true)
data class ChoiceMessage(
    @Json(name = "role")
    val role: String? = null,
    
    /**
     * 消息内容
     * 根据官方示例，可能是字符串或数组格式
     * 这里先按字符串处理，如果是数组需要进一步解析
     */
    @Json(name = "content")
    val content: String? = null
)

@JsonClass(generateAdapter = true)
data class UsageInfo(
    @Json(name = "prompt_tokens")
    val promptTokens: Int? = null,
    
    @Json(name = "completion_tokens")
    val completionTokens: Int? = null,
    
    @Json(name = "total_tokens")
    val totalTokens: Int? = null
)
