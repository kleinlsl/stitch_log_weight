package com.weighttracker.app.domain.model

data class WeightStats(
    val average: Double = 0.0,
    val max: Double = 0.0,
    val min: Double = 0.0,
    val count: Int = 0,
    val change: Double? = null
)
