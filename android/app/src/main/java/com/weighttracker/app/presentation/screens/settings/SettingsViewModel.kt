package com.weighttracker.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val goalWeight: Double = 70.0,
    val height: Double = 170.0,
    val startWeight: Double = 80.0,
    val isLoading: Boolean = true,
    val showGoalWeightDialog: Boolean = false,
    val showHeightDialog: Boolean = false,
    val showStartWeightDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            repository.goalWeight.collect { weight ->
                _uiState.update { it.copy(goalWeight = weight, isLoading = false) }
            }
        }
        viewModelScope.launch {
            repository.height.collect { h ->
                _uiState.update { it.copy(height = h) }
            }
        }
        viewModelScope.launch {
            repository.startWeight.collect { weight ->
                _uiState.update { it.copy(startWeight = weight) }
            }
        }
    }

    fun onGoalWeightChange(weight: String) {
        val value = weight.toDoubleOrNull() ?: return
        viewModelScope.launch {
            repository.setGoalWeight(value)
            _uiState.update { it.copy(goalWeight = value) }
        }
    }

    fun onHeightChange(height: String) {
        val value = height.toDoubleOrNull() ?: return
        viewModelScope.launch {
            repository.setHeight(value)
            _uiState.update { it.copy(height = value) }
        }
    }

    fun openGoalWeightDialog() {
        _uiState.update { it.copy(showGoalWeightDialog = true) }
    }

    fun closeGoalWeightDialog() {
        _uiState.update { it.copy(showGoalWeightDialog = false) }
    }

    fun confirmGoalWeight(value: String) {
        val weightValue = value.toDoubleOrNull() ?: return
        viewModelScope.launch {
            repository.setGoalWeight(weightValue)
            _uiState.update { it.copy(goalWeight = weightValue, showGoalWeightDialog = false) }
        }
    }

    fun openHeightDialog() {
        _uiState.update { it.copy(showHeightDialog = true) }
    }

    fun closeHeightDialog() {
        _uiState.update { it.copy(showHeightDialog = false) }
    }

    fun confirmHeight(value: String) {
        val heightValue = value.toDoubleOrNull() ?: return
        viewModelScope.launch {
            repository.setHeight(heightValue)
            _uiState.update { it.copy(height = heightValue, showHeightDialog = false) }
        }
    }

    fun openStartWeightDialog() {
        _uiState.update { it.copy(showStartWeightDialog = true) }
    }

    fun closeStartWeightDialog() {
        _uiState.update { it.copy(showStartWeightDialog = false) }
    }

    fun confirmStartWeight(value: String) {
        val weightValue = value.toDoubleOrNull() ?: return
        viewModelScope.launch {
            repository.setStartWeight(weightValue)
            _uiState.update { it.copy(startWeight = weightValue, showStartWeightDialog = false) }
        }
    }
}
