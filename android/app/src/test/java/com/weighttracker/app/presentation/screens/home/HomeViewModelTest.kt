package com.weighttracker.app.presentation.screens.home

import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.usecase.GetLatestRecordUseCase
import com.weighttracker.app.domain.usecase.GetWeightRecordsUseCase
import com.weighttracker.app.domain.usecase.GetWeeklyChangeUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var getLatestRecordUseCase: GetLatestRecordUseCase
    private lateinit var getWeightRecordsUseCase: GetWeightRecordsUseCase
    private lateinit var getWeeklyChangeUseCase: GetWeeklyChangeUseCase
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getLatestRecordUseCase = mockk()
        getWeightRecordsUseCase = mockk()
        getWeeklyChangeUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should load latest record on init`() = runTest {
        val latestRecord = WeightRecord(
            weight = 72.5,
            recordDate = "2026-03-22",
            recordTime = "08:30"
        )
        val weeklyRecords = listOf(
            WeightRecord(weight = 73.0, recordDate = "2026-03-21", recordTime = "08:30"),
            WeightRecord(weight = 72.5, recordDate = "2026-03-22", recordTime = "08:30")
        )

        coEvery { getLatestRecordUseCase() } returns flowOf(latestRecord)
        coEvery { getWeightRecordsUseCase.byDateRange(any(), any()) } returns flowOf(weeklyRecords)

        viewModel = HomeViewModel(
            getLatestRecordUseCase,
            getWeightRecordsUseCase,
            getWeeklyChangeUseCase
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(72.5, state.latestRecord?.weight ?: 0.0, 0.01)
        assertFalse(state.isLoading)
    }

    @Test
    fun `should show empty state when no records`() = runTest {
        coEvery { getLatestRecordUseCase() } returns flowOf(null)
        coEvery { getWeightRecordsUseCase.byDateRange(any(), any()) } returns flowOf(emptyList())

        viewModel = HomeViewModel(
            getLatestRecordUseCase,
            getWeightRecordsUseCase,
            getWeeklyChangeUseCase
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.latestRecord)
        assertFalse(state.isLoading)
    }
}
