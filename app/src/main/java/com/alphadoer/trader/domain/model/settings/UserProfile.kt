package com.alphadoer.trader.domain.model.settings

import com.squareup.moshi.JsonClass

/**
 * 用户基本信息
 */
@JsonClass(generateAdapter = true)
data class UserProfile(
    val userId: String,
    val nickname: String?,
    val avatarUri: String?,
    val bio: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
