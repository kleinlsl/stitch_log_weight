package com.weighttracker.app.presentation.screens.home

import com.weighttracker.app.domain.model.WeightRecord

data class HomeUiState(
    val latestRecord: WeightRecord? = null,
    val weeklyRecords: List<WeightRecord> = emptyList(),
    val weeklyChange: Double? = null,
    val bmi: Double? = null,
    val height: Double = 170.0,
    val goalWeight: Double = 70.0,
    val startWeight: Double = 80.0,
    val goalProgress: Float = 0f,
    val weightChange: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null
)
