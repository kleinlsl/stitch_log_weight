package com.weighttracker.app.presentation.screens.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.usecase.AddWeightRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val addWeightRecordUseCase: AddWeightRecordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntryUiState())
    val uiState: StateFlow<EntryUiState> = _uiState.asStateFlow()

    fun onWeightChange(weight: String) {
        _uiState.update { it.copy(weight = weight, error = null) }
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onTimeChange(time: LocalTime) {
        _uiState.update { it.copy(time = time) }
    }

    fun onMoodSelected(mood: Int) {
        _uiState.update { it.copy(mood = mood) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun saveRecord() {
        val state = _uiState.value
        val weightValue = state.weight.toDoubleOrNull() ?: return

        if (weightValue !in 20.0..300.0) {
            _uiState.update { it.copy(error = "体重必须在20-300kg之间") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val record = WeightRecord(
                weight = weightValue,
                recordDate = state.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                recordTime = String.format("%02d:%02d", state.time.hour, state.time.minute),
                mood = state.mood,
                note = state.note.takeIf { it.isNotBlank() }
            )

            addWeightRecordUseCase(record)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(isSaving = false, error = e.message ?: "保存失败") 
                    }
                }
        }
    }
}
