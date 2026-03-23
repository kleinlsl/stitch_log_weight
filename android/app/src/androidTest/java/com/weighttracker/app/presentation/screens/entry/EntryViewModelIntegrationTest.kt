package com.weighttracker.app.presentation.screens.entry

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.weighttracker.app.data.local.dao.WeightRecordDao
import com.weighttracker.app.data.local.database.AppDatabase
import com.weighttracker.app.data.repository.WeightRepositoryImpl
import com.weighttracker.app.domain.usecase.AddWeightRecordUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class EntryViewModelIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: WeightRecordDao
    private lateinit var repository: WeightRepositoryImpl
    private lateinit var addWeightRecordUseCase: AddWeightRecordUseCase
    private lateinit var viewModel: EntryViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = database.weightRecordDao()
        repository = WeightRepositoryImpl(dao)
        addWeightRecordUseCase = AddWeightRecordUseCase(repository)
        viewModel = EntryViewModel(addWeightRecordUseCase)
    }

    @After
    fun teardown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun initialState() {
        val state = viewModel.uiState.value
        assertEquals("", state.weight)
        assertEquals(LocalDate.now(), state.date)
        assertEquals(LocalTime.of(8, 30), state.time)
        assertNull(state.mood)
        assertEquals("", state.note)
        assertFalse(state.isSaving)
        assertFalse(state.saveSuccess)
        assertNull(state.error)
    }

    @Test
    fun canSaveWithValidWeight() {
        viewModel.onWeightChange("65.5")
        val state = viewModel.uiState.value
        assertTrue(state.canSave)
    }

    @Test
    fun cannotSaveWithEmptyWeight() {
        viewModel.onWeightChange("")
        val state = viewModel.uiState.value
        assertFalse(state.canSave)
    }

    @Test
    fun cannotSaveWithInvalidWeightTooLow() {
        viewModel.onWeightChange("15.0")
        val state = viewModel.uiState.value
        assertFalse(state.canSave)
    }

    @Test
    fun cannotSaveWithInvalidWeightTooHigh() {
        viewModel.onWeightChange("350.0")
        val state = viewModel.uiState.value
        assertFalse(state.canSave)
    }

    @Test
    fun onWeightChangeUpdatesState() {
        viewModel.onWeightChange("68.5")
        assertEquals("68.5", viewModel.uiState.value.weight)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun onDateChangeUpdatesState() {
        val newDate = LocalDate.of(2026, 3, 15)
        viewModel.onDateChange(newDate)
        assertEquals(newDate, viewModel.uiState.value.date)
    }

    @Test
    fun onTimeChangeUpdatesState() {
        val newTime = LocalTime.of(12, 30)
        viewModel.onTimeChange(newTime)
        assertEquals(newTime, viewModel.uiState.value.time)
    }

    @Test
    fun onMoodSelectedUpdatesState() {
        viewModel.onMoodSelected(4)
        assertEquals(4, viewModel.uiState.value.mood)
    }

    @Test
    fun onNoteChangeUpdatesState() {
        viewModel.onNoteChange("测试备注")
        assertEquals("测试备注", viewModel.uiState.value.note)
    }

    @Test
    fun saveRecordWithValidWeight() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.onWeightChange("65.5")
        viewModel.saveRecord()
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertTrue(state.saveSuccess)
        assertNull(state.error)
    }

    @Test
    fun saveRecordWithEmptyWeightDoesNothing() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.onWeightChange("")
        viewModel.saveRecord()
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertFalse(state.saveSuccess)
        assertNull(state.error)
    }

    @Test
    fun saveRecordWithInvalidWeightShowsError() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.onWeightChange("15.0")
        viewModel.saveRecord()
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertEquals("体重必须在20-300kg之间", state.error)
        assertFalse(state.saveSuccess)
    }

    @Test
    fun saveRecordWithBoundaryValues() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.onWeightChange("20.0")
        viewModel.saveRecord()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.saveSuccess)
        
        viewModel.onWeightChange("300.0")
        viewModel.saveRecord()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.saveSuccess)
    }
}
