package com.weighttracker.app.presentation.screens.entry

import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.usecase.AddWeightRecordUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class EntryViewModelTest {

    private lateinit var addWeightRecordUseCase: AddWeightRecordUseCase
    private lateinit var viewModel: EntryViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        addWeightRecordUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should update weight when input changes`() {
        viewModel = EntryViewModel(addWeightRecordUseCase)
        
        viewModel.onWeightChange("65.5")
        
        assertEquals("65.5", viewModel.uiState.value.weight)
    }

    @Test
    fun `should enable save button when weight is valid`() {
        viewModel = EntryViewModel(addWeightRecordUseCase)
        
        viewModel.onWeightChange("65.5")
        
        assertTrue(viewModel.uiState.value.canSave)
    }

    @Test
    fun `should disable save button when weight is empty`() {
        viewModel = EntryViewModel(addWeightRecordUseCase)
        
        viewModel.onWeightChange("")
        
        assertFalse(viewModel.uiState.value.canSave)
    }

    @Test
    fun `should disable save button when weight is invalid`() {
        viewModel = EntryViewModel(addWeightRecordUseCase)
        
        viewModel.onWeightChange("abc")
        
        assertFalse(viewModel.uiState.value.canSave)
    }

    @Test
    fun `should update mood when selected`() {
        viewModel = EntryViewModel(addWeightRecordUseCase)
        
        viewModel.onMoodSelected(3)
        
        assertEquals(3, viewModel.uiState.value.mood)
    }

    @Test
    fun `should update note when changed`() {
        viewModel = EntryViewModel(addWeightRecordUseCase)
        
        viewModel.onNoteChange("感觉很好")
        
        assertEquals("感觉很好", viewModel.uiState.value.note)
    }

    @Test
    fun `should save record successfully`() = runTest {
        coEvery { addWeightRecordUseCase(any()) } returns Result.success(1L)
        viewModel = EntryViewModel(addWeightRecordUseCase)
        
        viewModel.onWeightChange("65.5")
        viewModel.onMoodSelected(4)
        viewModel.onNoteChange("感觉很好")
        viewModel.saveRecord()
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value.saveSuccess)
    }
}
