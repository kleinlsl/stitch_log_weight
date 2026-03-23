package com.weighttracker.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeightRecordTest {

    @Test
    fun `should validate valid weight`() {
        val record = WeightRecord(
            weight = 65.5,
            recordDate = "2026-03-22",
            recordTime = "08:30"
        )
        assertTrue(record.isValid())
    }

    @Test
    fun `should reject weight too low`() {
        val record = WeightRecord(
            weight = 15.0,
            recordDate = "2026-03-22",
            recordTime = "08:30"
        )
        assertFalse(record.isValid())
    }

    @Test
    fun `should reject weight too high`() {
        val record = WeightRecord(
            weight = 350.0,
            recordDate = "2026-03-22",
            recordTime = "08:30"
        )
        assertFalse(record.isValid())
    }

    @Test
    fun `should return KG as weight unit`() {
        val record = WeightRecord(
            weight = 65.5,
            recordDate = "2026-03-22",
            recordTime = "08:30"
        )
        assertEquals("KG", record.weightUnit)
    }

    @Test
    fun `should accept boundary weight values`() {
        val minValid = WeightRecord(weight = 20.0, recordDate = "2026-03-22", recordTime = "08:30")
        val maxValid = WeightRecord(weight = 300.0, recordDate = "2026-03-22", recordTime = "08:30")
        
        assertTrue(minValid.isValid())
        assertTrue(maxValid.isValid())
    }
}
