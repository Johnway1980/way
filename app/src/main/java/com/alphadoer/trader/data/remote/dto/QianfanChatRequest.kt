package com.alphadoer.trader.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 百度千帆 Chat Completions API 请求格式
 * 根据官方示例：https://qianfan.baidubce.com/v2/chat/completions
 */
@JsonClass(generateAdapter = true)
data class QianfanChatRequest(
    /**
     * 使用的模型名称
     * 根据官方示例使用：ernie-4.5-turbo-vl-latest
     */
    @Json(name = "model")
    val model: String = "ernie-4.5-turbo-vl-latest",

    @Json(name = "messages")
    val messages: List<ChatMessage>,

    /**
     * 是否流式返回，根据官方示例设置为 false
     */
    @Json(name = "stream")
    val stream: Boolean = false,

    @Json(name = "temperature")
    val temperature: Double = 0.7,

    @Json(name = "top_p")
    val topP: Double = 0.8,

    @Json(name = "penalty_score")
    val penaltyScore: Double = 1.0
)

/**
 * 聊天消息
 * 根据官方示例，content 是数组格式：[{"type": "text", "text": "..."}]
 */
@JsonClass(generateAdapter = true)
data class ChatMessage(
    @Json(name = "role")
    val role: String, // "user" | "assistant" | "system"
    
    /**
     * 消息内容，根据官方示例是数组格式
     * 每个元素包含 type 和 text 字段
     */
    @Json(name = "content")
    val content: List<ContentItem>
)

/**
 * 消息内容项
 * 根据官方示例格式：{"type": "text", "text": "实际文本内容"}
 */
@JsonClass(generateAdapter = true)
data class ContentItem(
    @Json(name = "type")
    val type: String = "text",
    
    @Json(name = "text")
    val text: String
)
