package com.alphadoer.trader.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

/**
 * 错误处理拦截器
 * 统一处理HTTP错误响应
 */
class ErrorHandlingInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            throw NetworkException("网络连接失败: ${e.message}", e)
        }
        
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "未知错误"
            throw HttpException(
                code = response.code,
                message = response.message,
                body = errorBody
            )
        }
        
        return response
    }
}

/**
 * 网络异常
 */
class NetworkException(
    message: String,
    cause: Throwable? = null
) : IOException(message, cause)

/**
 * HTTP异常
 */
class HttpException(
    val code: Int,
    message: String,
    val body: String
) : IOException("HTTP $code: $message")
