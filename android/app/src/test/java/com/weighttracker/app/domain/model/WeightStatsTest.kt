package com.weighttracker.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WeightStatsTest {

    @Test
    fun `should have default values`() {
        val stats = WeightStats()
        
        assertEquals(0.0, stats.average, 0.01)
        assertEquals(0.0, stats.max, 0.01)
        assertEquals(0.0, stats.min, 0.01)
        assertEquals(0, stats.count)
        assertEquals(null, stats.change)
    }

    @Test
    fun `should store calculated values`() {
        val stats = WeightStats(
            average = 68.5,
            max = 70.0,
            min = 65.0,
            count = 10,
            change = -2.5
        )
        
        assertEquals(68.5, stats.average, 0.01)
        assertEquals(70.0, stats.max, 0.01)
        assertEquals(65.0, stats.min, 0.01)
        assertEquals(10, stats.count)
        assertEquals(-2.5, stats.change!!, 0.01)
    }
}
