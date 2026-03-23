package com.weighttracker.app.domain.usecase

import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.repository.WeightRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetWeightStatsUseCaseTest {

    private lateinit var repository: WeightRepository
    private lateinit var useCase: GetWeightStatsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetWeightStatsUseCase(repository)
    }

    @Test
    fun `should calculate correct stats`() {
        val records = listOf(
            WeightRecord(weight = 70.0, recordDate = "2026-03-15", recordTime = "08:00"),
            WeightRecord(weight = 69.5, recordDate = "2026-03-16", recordTime = "08:00"),
            WeightRecord(weight = 69.0, recordDate = "2026-03-17", recordTime = "08:00"),
            WeightRecord(weight = 68.5, recordDate = "2026-03-18", recordTime = "08:00"),
            WeightRecord(weight = 68.0, recordDate = "2026-03-19", recordTime = "08:00")
        )

        val stats = useCase(records)

        assertEquals(69.0, stats.average, 0.01)
        assertEquals(70.0, stats.max, 0.01)
        assertEquals(68.0, stats.min, 0.01)
        assertEquals(5, stats.count)
    }

    @Test
    fun `should return empty stats for empty list`() {
        val stats = useCase(emptyList())

        assertEquals(0.0, stats.average, 0.01)
        assertEquals(0.0, stats.max, 0.01)
        assertEquals(0.0, stats.min, 0.01)
        assertEquals(0, stats.count)
    }

    @Test
    fun `should handle single record`() {
        val records = listOf(
            WeightRecord(weight = 65.5, recordDate = "2026-03-22", recordTime = "08:00")
        )

        val stats = useCase(records)

        assertEquals(65.5, stats.average, 0.01)
        assertEquals(65.5, stats.max, 0.01)
        assertEquals(65.5, stats.min, 0.01)
        assertEquals(1, stats.count)
    }
}
