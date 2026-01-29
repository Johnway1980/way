package com.alphadoer.trader.data.util

import android.util.Log

/**
 * 简易性能计时器，用于关键路径的耗时记录。
 */
object PerformanceTracker {
    data class TimedResult<T>(val value: T, val durationMs: Long)

    fun <T> measure(label: String, block: () -> T): TimedResult<T> {
        val start = System.nanoTime()
        val result = block()
        val end = System.nanoTime()
        val duration = (end - start) / 1_000_000
        try {
            Log.d("PerformanceTracker", "$label 耗时 ${duration}ms")
        } catch (t: Throwable) {
            // 在单元测试环境中 android.util.Log 可能不可用，忽略日志写入错误
        }
        return TimedResult(result, duration)
    }
}
