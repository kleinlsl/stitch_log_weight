package com.weighttracker.app.domain.usecase

import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.repository.WeightRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddWeightRecordUseCaseTest {

    private lateinit var repository: WeightRepository
    private lateinit var useCase: AddWeightRecordUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = AddWeightRecordUseCase(repository)
    }

    @Test
    fun `should save valid weight record`() = runTest {
        val record = WeightRecord(
            weight = 65.5,
            recordDate = "2026-03-22",
            recordTime = "08:30",
            mood = 3
        )
        coEvery { repository.addRecord(record) } returns Result.success(1L)

        val result = useCase(record)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun `should reject invalid weight too low`() = runTest {
        val record = WeightRecord(
            weight = 10.0,
            recordDate = "2026-03-22",
            recordTime = "08:30"
        )

        val result = useCase(record)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `should reject invalid weight too high`() = runTest {
        val record = WeightRecord(
            weight = 350.0,
            recordDate = "2026-03-22",
            recordTime = "08:30"
        )

        val result = useCase(record)

        assertTrue(result.isFailure)
    }
}
