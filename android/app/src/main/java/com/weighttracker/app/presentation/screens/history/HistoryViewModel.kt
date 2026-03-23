package com.weighttracker.app.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.repository.WeightRepository
import com.weighttracker.app.domain.usecase.DeleteRecordUseCase
import com.weighttracker.app.domain.usecase.UpdateRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MonthGroup(
    val month: String,
    val records: List<WeightRecord>
)

data class EditState(
    val isEditing: Boolean = false,
    val weight: String = "",
    val date: String = "",
    val time: String = "",
    val mood: Int? = null,
    val note: String = ""
)

data class HistoryUiState(
    val groupedRecords: List<MonthGroup> = emptyList(),
    val selectedRecord: WeightRecord? = null,
    val isLoading: Boolean = true,
    val showDeleteDialog: Boolean = false,
    val recordToDelete: WeightRecord? = null,
    val editState: EditState = EditState()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: WeightRepository,
    private val deleteRecordUseCase: DeleteRecordUseCase,
    private val updateRecordUseCase: UpdateRecordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadRecords()
    }

    private fun loadRecords() {
        viewModelScope.launch {
            repository.getAllRecords().collect { records ->
                val grouped = groupByMonth(records)
                _uiState.update {
                    it.copy(
                        groupedRecords = grouped,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun groupByMonth(records: List<WeightRecord>): List<MonthGroup> {
        return records
            .groupBy { record ->
                val parts = record.recordDate.split("-")
                if (parts.size >= 2) {
                    "${parts[0]}年${parts[1]}月"
                } else {
                    "未知"
                }
            }
            .map { (month, monthRecords) ->
                MonthGroup(month = month, records = monthRecords)
            }
            .sortedByDescending { it.month }
    }

    fun onRecordClick(record: WeightRecord) {
        _uiState.update { it.copy(selectedRecord = record) }
    }

    fun onDismissDetail() {
        _uiState.update { it.copy(selectedRecord = null, editState = EditState()) }
    }

    fun startEditing(record: WeightRecord) {
        _uiState.update {
            it.copy(
                editState = EditState(
                    isEditing = true,
                    weight = record.weight.toString(),
                    date = record.recordDate,
                    time = record.recordTime,
                    mood = record.mood,
                    note = record.note ?: ""
                )
            )
        }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(editState = EditState()) }
    }

    fun onEditWeightChange(weight: String) {
        _uiState.update { it.copy(editState = it.editState.copy(weight = weight)) }
    }

    fun onEditDateChange(date: String) {
        _uiState.update { it.copy(editState = it.editState.copy(date = date)) }
    }

    fun onEditTimeChange(time: String) {
        _uiState.update { it.copy(editState = it.editState.copy(time = time)) }
    }

    fun onEditMoodChange(mood: Int?) {
        _uiState.update { it.copy(editState = it.editState.copy(mood = mood)) }
    }

    fun onEditNoteChange(note: String) {
        _uiState.update { it.copy(editState = it.editState.copy(note = note)) }
    }

    fun saveEdit() {
        val state = _uiState.value
        val record = state.selectedRecord ?: return
        val edit = state.editState
        val weightValue = edit.weight.toDoubleOrNull() ?: return

        if (weightValue !in 20.0..300.0) return

        viewModelScope.launch {
            val updatedRecord = record.copy(
                weight = weightValue,
                recordDate = edit.date,
                recordTime = edit.time,
                mood = edit.mood,
                note = edit.note.takeIf { it.isNotBlank() }
            )
            updateRecordUseCase(updatedRecord)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            editState = EditState(),
                            selectedRecord = updatedRecord
                        )
                    }
                }
        }
    }

    fun onDeleteClick(record: WeightRecord) {
        _uiState.update {
            it.copy(
                showDeleteDialog = true,
                recordToDelete = record
            )
        }
    }

    fun onConfirmDelete() {
        val record = _uiState.value.recordToDelete ?: return
        viewModelScope.launch {
            deleteRecordUseCase(record)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showDeleteDialog = false,
                            recordToDelete = null,
                            selectedRecord = null
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            showDeleteDialog = false,
                            recordToDelete = null
                        )
                    }
                }
        }
    }

    fun onDismissDeleteDialog() {
        _uiState.update {
            it.copy(
                showDeleteDialog = false,
                recordToDelete = null
            )
        }
    }
}
