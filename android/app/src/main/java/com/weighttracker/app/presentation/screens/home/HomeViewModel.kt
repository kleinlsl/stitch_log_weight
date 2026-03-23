package com.weighttracker.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weighttracker.app.domain.model.WeightRecord
import com.weighttracker.app.domain.usecase.GetLatestRecordUseCase
import com.weighttracker.app.domain.usecase.GetWeightRecordsUseCase
import com.weighttracker.app.domain.usecase.GetWeeklyChangeUseCase
import com.weighttracker.app.presentation.screens.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getLatestRecordUseCase: GetLatestRecordUseCase,
    private val getWeightRecordsUseCase: GetWeightRecordsUseCase,
    private val getWeeklyChangeUseCase: GetWeeklyChangeUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val height = settingsRepository.height.first()
            val goalWeight = settingsRepository.goalWeight.first()
            val startWeight = settingsRepository.startWeight.first()
            val today = LocalDate.now()
            val weekAgo = today.minusDays(7)
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE

            combine(
                getLatestRecordUseCase(),
                getWeightRecordsUseCase.byDateRange(
                    weekAgo.format(formatter),
                    today.format(formatter)
                )
            ) { latest, weekly ->
                val weeklyChange = if (weekly.size >= 2) {
                    val sorted = weekly.sortedByDescending { it.recordDate }
                    sorted.firstOrNull()?.weight?.minus(sorted.getOrNull(1)?.weight ?: 0.0)
                } else null

                val bmi = latest?.let { calculateBMI(it.weight, height) }
                val goalProgress = latest?.let { 
                    calculateGoalProgress(it.weight, goalWeight, startWeight) 
                } ?: 0f
                val weightChange = latest?.let { it.weight - startWeight } ?: 0.0

                HomeUiState(
                    latestRecord = latest,
                    weeklyRecords = weekly,
                    weeklyChange = weeklyChange,
                    bmi = bmi,
                    height = height,
                    goalWeight = goalWeight,
                    startWeight = startWeight,
                    goalProgress = goalProgress,
                    weightChange = weightChange,
                    isLoading = false
                )
            }
            .catch { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
            .collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun calculateBMI(weight: Double, heightCm: Double): Double {
        val heightM = heightCm / 100
        return weight / (heightM * heightM)
    }

    private fun calculateGoalProgress(currentWeight: Double, goalWeight: Double, startWeight: Double): Float {
        val total = startWeight - goalWeight
        val current = startWeight - currentWeight
        return if (total > 0) (current / total).toFloat().coerceIn(0f, 1f) else 0f
    }
}
