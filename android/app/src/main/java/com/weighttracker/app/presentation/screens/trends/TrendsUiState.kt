package com.weighttracker.app.presentation.screens.trends

import com.weighttracker.app.domain.model.TimeRange
import com.weighttracker.app.domain.model.WeightRecord

data class TrendsUiState(
    val timeRange: TimeRange = TimeRange.WEEK,
    val records: List<WeightRecord> = emptyList(),
    val averageWeight: Double = 0.0,
    val maxWeight: Double = 0.0,
    val minWeight: Double = 0.0,
    val change: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null
)
