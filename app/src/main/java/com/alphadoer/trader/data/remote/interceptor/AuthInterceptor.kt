package com.alphadoer.trader.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 认证拦截器
 * 用于添加API认证token等
 */
class AuthInterceptor(
    private val tokenProvider: () -> String? = { null }
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val token = tokenProvider()
        val requestBuilder = originalRequest.newBuilder()
        
        // 根据官方示例，需要添加 appid header（即使为空）
        requestBuilder.header("appid", "")
        
        // 根据官方示例，Authorization header 格式：Bearer {token}
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        
        return chain.proceed(requestBuilder.build())
    }
}
