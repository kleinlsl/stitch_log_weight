package com.weighttracker.app.domain.usecase

import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.repository.WeightRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetLatestRecordUseCaseTest {

    private lateinit var repository: WeightRepository
    private lateinit var useCase: GetLatestRecordUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetLatestRecordUseCase(repository)
    }

    @Test
    fun `should return latest record`() = runTest {
        val latestRecord = WeightRecord(
            id = 1,
            weight = 72.5,
            recordDate = "2026-03-22",
            recordTime = "08:30"
        )
        coEvery { repository.getLatestRecord() } returns flowOf(latestRecord)

        useCase().collect { record ->
            assertEquals(72.5, record?.weight ?: 0.0, 0.01)
        }
    }

    @Test
    fun `should return null when no records`() = runTest {
        coEvery { repository.getLatestRecord() } returns flowOf(null)

        useCase().collect { record ->
            assertNull(record)
        }
    }
}
