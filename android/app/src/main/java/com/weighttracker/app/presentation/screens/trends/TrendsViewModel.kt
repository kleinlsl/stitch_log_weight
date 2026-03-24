package com.weighttracker.app.presentation.screens.trends

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weighttracker.app.domain.model.ChartMode
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

    fun onChartModeToggle() {
        val newMode = when (_uiState.value.chartMode) {
            ChartMode.SCROLL -> ChartMode.OVERVIEW
            ChartMode.OVERVIEW -> ChartMode.SCROLL
        }
        _uiState.value = _uiState.value.copy(chartMode = newMode)
    }

    fun shareTrendData(context: Context) {
        val state = _uiState.value
        val rangeLabel = when (state.timeRange) {
            TimeRange.WEEK -> "近一周"
            TimeRange.MONTH -> "近一月"
            TimeRange.YEAR -> "近一年"
        }
        
        val shareText = buildString {
            appendLine("📊 体重趋势报告 ($rangeLabel)")
            appendLine()
            appendLine("平均体重: ${String.format("%.1f", state.averageWeight)} kg")
            appendLine("最高体重: ${String.format("%.1f", state.maxWeight)} kg")
            appendLine("最低体重: ${String.format("%.1f", state.minWeight)} kg")
            if (state.change != 0.0) {
                val changeText = if (state.change < 0) "↓" else "↑"
                appendLine("变化: $changeText ${String.format("%.1f", kotlin.math.abs(state.change))} kg")
            }
            appendLine()
            appendLine("最近记录:")
            state.records.take(5).forEach { record ->
                appendLine("• ${record.recordDate} ${record.recordTime} - ${record.weight} kg")
            }
            appendLine()
            appendLine("来自「体重记录」App")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "分享趋势数据"))
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
