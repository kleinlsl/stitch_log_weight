package com.weighttracker.app.presentation.screens.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weighttracker.app.domain.model.TimeRange
import com.weighttracker.app.domain.usecase.GetWeightRecordsUseCase
import com.weighttracker.app.domain.usecase.GetWeightStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val getWeightRecordsUseCase: GetWeightRecordsUseCase,
    private val getWeightStatsUseCase: GetWeightStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrendsUiState())
    val uiState: StateFlow<TrendsUiState> = _uiState.asStateFlow()

    init {
        loadData(TimeRange.WEEK)
    }

    fun onTimeRangeChange(range: TimeRange) {
        _uiState.value = _uiState.value.copy(timeRange = range, isLoading = true)
        loadData(range)
    }

    private fun loadData(range: TimeRange) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val startDate = when (range) {
                TimeRange.WEEK -> today.minusDays(7)
                TimeRange.MONTH -> today.minusDays(30)
                TimeRange.YEAR -> today.minusDays(365)
            }
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE

            getWeightRecordsUseCase.byDateRange(
                startDate.format(formatter),
                today.format(formatter)
            )
            .catch { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
            .collect { records ->
                val stats = getWeightStatsUseCase(records)
                val change = if (records.size >= 2) {
                    records.maxByOrNull { it.recordDate }?.weight?.minus(
                        records.sortedByDescending { it.recordDate }.getOrNull(1)?.weight ?: 0.0
                    ) ?: 0.0
                } else 0.0

                _uiState.value = _uiState.value.copy(
                    records = records,
                    averageWeight = stats.average,
                    maxWeight = stats.max,
                    minWeight = stats.min,
                    change = change,
                    isLoading = false
                )
            }
        }
    }
}
