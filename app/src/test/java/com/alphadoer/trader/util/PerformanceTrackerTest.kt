package com.alphadoer.trader.util

import com.alphadoer.trader.data.util.PerformanceTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceTrackerTest {

    @Test
    fun `measure returns value and non-negative duration`() {
        val result = PerformanceTracker.measure("dummy") {
            2 + 3
        }
        assertEquals(5, result.value)
        assertTrue(result.durationMs >= 0)
    }
}
